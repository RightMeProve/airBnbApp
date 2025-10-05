package com.rightmeprove.airbnb.airBnbApp.service;

import com.rightmeprove.airbnb.airBnbApp.dto.BookingDto;
import com.rightmeprove.airbnb.airBnbApp.dto.BookingRequestDto;
import com.rightmeprove.airbnb.airBnbApp.dto.GuestDto;
import com.rightmeprove.airbnb.airBnbApp.dto.HotelReportDto;
import com.rightmeprove.airbnb.airBnbApp.entity.*;
import com.rightmeprove.airbnb.airBnbApp.entity.enums.BookingStatus;
import com.rightmeprove.airbnb.airBnbApp.exception.ResourceNotFoundException;
import com.rightmeprove.airbnb.airBnbApp.exception.UnAuthorisedException;
import com.rightmeprove.airbnb.airBnbApp.repository.*;
import com.rightmeprove.airbnb.airBnbApp.strategy.PricingService;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
/*
 * Service implementation for booking management.
 * Handles:
 * - Initializing bookings (locking inventory, reserving rooms)
 * - Adding guests to a booking
 * - Checking expiry rules
 */
public class BookingServiceImpl implements BookingService {

    // Dependencies injected via constructor (thanks to @RequiredArgsConstructor)
    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final InventoryRepository inventoryRepository;
    private final ModelMapper modelMapper;
    private final GuestRespository guestRespository;
    private final CheckoutService checkoutService;
    private final PricingService pricingService;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    @Transactional // All DB operations here are atomic (rollback on failure)
    public BookingDto initialiseBooking(BookingRequestDto bookingRequest) {
        log.info("Initialising Booking for hotel: {}, room: {}, date: {}-{}",
                bookingRequest.getHotelId(),
                bookingRequest.getRoomId(),
                bookingRequest.getCheckInDate(),
                bookingRequest.getCheckOutDate());

        // Validate hotel exists
        Hotel hotel = hotelRepository.findById(bookingRequest.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Hotel not found with ID: " + bookingRequest.getHotelId()));

        // Validate room exists
        Room room = roomRepository.findById(bookingRequest.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Room not found with ID: " + bookingRequest.getRoomId()));

        // Fetch and lock inventory rows (pessimistic lock prevents overbooking)
        List<Inventory> inventoryList = inventoryRepository.findAndLockAvailableInventory(
                room.getId(),
                bookingRequest.getCheckInDate(),
                bookingRequest.getCheckOutDate(),
                bookingRequest.getRoomsCount()
        );

        // Calculate how many days the user wants to book
        long daysCount = ChronoUnit.DAYS.between(
                bookingRequest.getCheckInDate(),
                bookingRequest.getCheckOutDate()
        ) + 1;

        // Ensure inventory is available for ALL days in range
        if (inventoryList.size() != daysCount) {
            throw new IllegalStateException("Room is not available for the entire stay duration");
        }

        // Reserve rooms → increase reservedCount for each day
        inventoryRepository.initBooking(room.getId(),bookingRequest.getCheckInDate(),bookingRequest.getCheckOutDate()
        ,bookingRequest.getRoomsCount());

        BigDecimal priceForOneRoom = pricingService.calculateTotalPrice(inventoryList);
        BigDecimal totalPrice = priceForOneRoom.multiply(BigDecimal.valueOf(bookingRequest.getRoomsCount()));

        // Create booking with RESERVED status
        Booking booking = Booking.builder()
                .bookingStatus(BookingStatus.RESERVED)
                .hotel(hotel)
                .room(room)
                .checkInDate(bookingRequest.getCheckInDate())
                .checkOutDate(bookingRequest.getCheckOutDate())
                .user(getCurrentUser()) // TODO: integrate with real logged-in user
                .roomsCount(bookingRequest.getRoomsCount())
                .amount(totalPrice)
                .build();

        booking = bookingRepository.save(booking);
        return modelMapper.map(booking, BookingDto.class);
    }

    @Override
    @Transactional
    public BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList) {
        log.info("Adding guests for booking with id: {}", bookingId);

        // Fetch booking or fail if not found
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new ResourceNotFoundException("Booking not found with id: " + bookingId));

        User user = getCurrentUser();

        if(!user.equals(booking.getUser()))
        {
            throw new UnAuthorisedException("Booking does not belong to this user with id: "+user.getId());
        }

        // Block if booking expired (older than 10 minutes)
        if (hasBookingExpired(booking)) {
            throw new IllegalStateException("Booking has already expired");
        }

        // Only allowed if booking is still RESERVED
        if (booking.getBookingStatus() != BookingStatus.RESERVED) {
            throw new IllegalStateException("Booking is not in RESERVED state, cannot add guests");
        }

        // Convert GuestDto → Guest entity, link guests to booking
        for (GuestDto guestDto : guestDtoList) {
            Guest guest = modelMapper.map(guestDto, Guest.class);
            guest.setUser(user);
            guest = guestRespository.save(guest);
            booking.getGuests().add(guest);
        }

        // Update booking status after guests added
        booking.setBookingStatus(BookingStatus.GUESTS_ADDED);
        booking = bookingRepository.save(booking);
        return modelMapper.map(booking, BookingDto.class);
    }

    @Override
    @Transactional
    public String initiatePayments(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                ()-> new ResourceNotFoundException("Booking not found with id: "+bookingId)
        );
        User user = getCurrentUser();
        if(!user.equals(booking.getUser()))
        {
            throw new UnAuthorisedException("Booking does not belong to this user with id: "+user.getId());
        }

        if(hasBookingExpired(booking))
        {
            throw new IllegalStateException("Booking has already expired.");
        }

        String sessionUrl = checkoutService.getCheckoutSession(booking,
                frontendUrl+"/payments/success",frontendUrl+"/payments/failure");

        booking.setBookingStatus(BookingStatus.PAYMENT_PENDING);
        bookingRepository.save(booking);
        return sessionUrl;
    }

    @Override
    @Transactional
    public void capturePayment(Event event) {
        if("checkout.session.completed".equals(event.getType()))
        {
            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
            if(session == null) return;

            String sessionId = session.getId();
            Booking booking = bookingRepository.findByPaymentSessionId(sessionId)
                    .orElseThrow(()-> new ResourceNotFoundException("Booking not found for session ID: "+sessionId));

            booking.setBookingStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);

            inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(),booking.getCheckInDate(),
                    booking.getCheckOutDate(),booking.getRoomsCount());

            inventoryRepository.confirmBooking(booking.getRoom().getId(),booking.getCheckInDate(),
                    booking.getCheckOutDate(),booking.getRoomsCount());

            log.info("Successfully Confirmed the booking for booking ID: {}",booking.getId());
        }
        else {
            log.warn("Unhandled event type: {}",event.getType());
        }
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                ()-> new ResourceNotFoundException("Booking not found with ID: "+bookingId)
        );

        User user = getCurrentUser();
        if(!user.equals(booking.getUser()))
        {
            throw new UnAuthorisedException("Booking does not belong to this user with id: "+user.getId());
        }

        if(booking.getBookingStatus() != BookingStatus.CONFIRMED){
            throw new IllegalStateException("Only confirmed bookings can be cancelled");
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(),booking.getCheckInDate(),
                booking.getCheckOutDate(),booking.getRoomsCount());

        inventoryRepository.cancelBooking(booking.getRoom().getId(),booking.getCheckInDate(),booking.getCheckOutDate(),
                booking.getRoomsCount());

        // handle the refund

        try{
            Session session = Session.retrieve(booking.getPaymentSessionId());
            RefundCreateParams refundParams = RefundCreateParams.builder()
                    .setPaymentIntent(session.getPaymentIntent())
                    .build();
            Refund.create(refundParams);

        }catch (StripeException e){
            throw new RuntimeException(e);
        }

    }

    @Override
    public List<BookingDto> getAllBookingsByHotelId(Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(()->new ResourceNotFoundException("Hotel is not found with" +
                "hotel ID:  {}"+hotelId));

        User user = getCurrentUser();

        log.info("Getting all hotels for the hotel with ID: {}",hotelId);

        if(!user.equals(hotel.getOwner()))
        {
            throw new AccessDeniedException("You are not the owner of this Hotel with ID: "+hotelId);
        }

        List<Booking> bookings = bookingRepository.findByHotel(hotel);

        return bookings.stream()
                .map((element)->modelMapper.map(element,BookingDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public HotelReportDto getHotelReport(Long hotelId, LocalDate startDate, LocalDate endDate) {
        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(()->new ResourceNotFoundException("Hotel" +
                "is not found with id: "+hotelId));

        User user = getCurrentUser();

        log.info("Geting report for the hotel with ID: {}",hotelId);

        if(!user.equals(hotel.getOwner()))
        {
            throw new AccessDeniedException("You are not the owner of this Hotel with ID: "+hotelId);
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Booking> bookings = bookingRepository.findByHotelAndCreatedAtBetween(hotel,startDateTime,endDateTime);

        Long totalConfirmedBookings = bookings
                .stream()
                .filter(booking -> booking.getBookingStatus() == BookingStatus.CONFIRMED)
                .count();

        BigDecimal totalRevenueOfConfirmedBookings = bookings.stream()
                .filter(booking -> booking.getBookingStatus() == BookingStatus.CONFIRMED)
                .map(Booking::getAmount)
                .reduce(BigDecimal.ZERO,BigDecimal::add);

        BigDecimal avgRevenue = totalConfirmedBookings == 0? BigDecimal.ZERO :
                totalRevenueOfConfirmedBookings.divide(BigDecimal.valueOf(totalConfirmedBookings), RoundingMode.HALF_UP);

        return new HotelReportDto(totalConfirmedBookings,totalRevenueOfConfirmedBookings,avgRevenue);

    }

    // Helper: check if booking has expired (> 10 minutes since creation)
    public Boolean hasBookingExpired(Booking booking) {
        return booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
    }

    // TEMP: mock current user (replace with real authentication later)
    public User getCurrentUser() {
       return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
