package com.rightmeprove.airbnb.airBnbApp.controller;

import com.rightmeprove.airbnb.airBnbApp.dto.BookingDto;
import com.rightmeprove.airbnb.airBnbApp.dto.BookingRequestDto;
import com.rightmeprove.airbnb.airBnbApp.dto.GuestDto;
import com.rightmeprove.airbnb.airBnbApp.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor // Generates constructor for final fields (injects BookingService)
@RequestMapping("/bookings") // Base route for all booking-related operations
public class HotelBookingController {

    private final BookingService bookingService;

    /**
     * Initializes a new booking request (creates a pending booking entry).
     * @param bookingRequest details like roomId, check-in/out dates, etc.
     * @return booking summary (BookingDto)
     */
    @PostMapping("/init")
    public ResponseEntity<BookingDto> initialiseBooking(@RequestBody BookingRequestDto bookingRequest) {
        return ResponseEntity.ok(bookingService.initialiseBooking(bookingRequest));
    }

    /**
     * Adds guests to an existing booking.
     * @param bookingId booking to which guests are added
     * @param guestDtoList list of guests (names, ages, etc.)
     * @return updated BookingDto including guest info
     */
    @PostMapping("/{bookingId}/addGuests")
    public ResponseEntity<BookingDto> addGuests(@PathVariable Long bookingId, @RequestBody List<GuestDto> guestDtoList) {
        return ResponseEntity.ok(bookingService.addGuests(bookingId, guestDtoList));
    }

    /**
     * Initiates Stripe payment for a specific booking.
     * @param bookingId ID of the booking to pay for
     * @return Stripe checkout session URL
     */
    @PostMapping("/{bookingId}/payments")
    public ResponseEntity<Map<String, String>> initiatePayment(@PathVariable Long bookingId) {
        String sessionUrl = bookingService.initiatePayments(bookingId);
        return ResponseEntity.ok(Map.of("sessionUrl", sessionUrl));
    }

    /**
     * Cancels an existing booking.
     * @param bookingId ID of the booking to cancel
     * @return no content response on success
     */
    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long bookingId) {
        bookingService.cancelBooking(bookingId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}
