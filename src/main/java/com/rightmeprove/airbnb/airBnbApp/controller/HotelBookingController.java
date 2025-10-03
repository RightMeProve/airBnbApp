package com.rightmeprove.airbnb.airBnbApp.controller;

import com.rightmeprove.airbnb.airBnbApp.dto.BookingDto;
import com.rightmeprove.airbnb.airBnbApp.dto.BookingRequestDto;
import com.rightmeprove.airbnb.airBnbApp.dto.GuestDto;
import com.rightmeprove.airbnb.airBnbApp.entity.Booking;
import com.rightmeprove.airbnb.airBnbApp.service.BookingService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bookings")
public class HotelBookingController {

    private final BookingService bookingService;

    @PostMapping("/init")
    public ResponseEntity<BookingDto> initialiseBooking(@RequestBody BookingRequestDto bookingRequest){
        return ResponseEntity.ok(bookingService.initialiseBooking(bookingRequest));
    }

    @PostMapping("/{bookingId}/addGuests")
    public ResponseEntity<BookingDto> addGuests( @PathVariable Long bookingId,@RequestBody List<GuestDto> guestDtoList){
        return ResponseEntity.ok(bookingService.addGuests(bookingId,guestDtoList));
    }

    @PostMapping("/{bookingId}/payments")
    public ResponseEntity<Map<String,String>> initiatePayment(@PathVariable Long bookingId){
        String sessionUrl = bookingService.initiatePayments(bookingId);

    }
}
