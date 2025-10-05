package com.rightmeprove.airbnb.airBnbApp.repository;

import com.rightmeprove.airbnb.airBnbApp.entity.Booking;
import com.rightmeprove.airbnb.airBnbApp.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking,Long> {

    Optional<Booking> findByPaymentSessionId(String sessionId);

    List<Booking> findByHotel(Hotel hotel);
}
