package com.rightmeprove.airbnb.airBnbApp.repository;

import com.rightmeprove.airbnb.airBnbApp.entity.Booking;
import com.rightmeprove.airbnb.airBnbApp.entity.Hotel;
import com.rightmeprove.airbnb.airBnbApp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Booking entity.
 * Extends JpaRepository → provides CRUD + pagination/sorting out-of-the-box.
 */
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Find a booking by its Stripe payment session ID
    Optional<Booking> findByPaymentSessionId(String sessionId);

    // Find all bookings for a specific hotel
    List<Booking> findByHotel(Hotel hotel);

    // Find all bookings for a hotel created within a specific datetime range
    List<Booking> findByHotelAndCreatedAtBetween(Hotel hotel, LocalDateTime startDateTime, LocalDateTime endDateTime);

    // Find all bookings made by a specific user
    List<Booking> findByUser(User user);
}
