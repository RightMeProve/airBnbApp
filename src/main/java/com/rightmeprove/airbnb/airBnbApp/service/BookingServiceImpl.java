package com.rightmeprove.airbnb.airBnbApp.service;

import com.rightmeprove.airbnb.airBnbApp.dto.BookingDto;
import com.rightmeprove.airbnb.airBnbApp.dto.BookingRequestDto;
import com.rightmeprove.airbnb.airBnbApp.dto.GuestDto;
import com.rightmeprove.airbnb.airBnbApp.entity.*;
import com.rightmeprove.airbnb.airBnbApp.entity.enums.BookingStatus;
import com.rightmeprove.airbnb.airBnbApp.exception.ResourceNotFoundException;
import com.rightmeprove.airbnb.airBnbApp.exception.UnAuthorisedException;
import com.rightmeprove.airbnb.airBnbApp.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

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
        for (Inventory inventory : inventoryList) {
            inventory.setReservedCount(
                    inventory.getReservedCount() + bookingRequest.getRoomsCount()
            );
        }
        inventoryRepository.saveAll(inventoryList);

        // TODO: implement dynamic pricing logic (currently dummy value)
        BigDecimal totalPrice = BigDecimal.TEN;

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

        return "";
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
