package com.rightmeprove.airbnb.airBnbApp.repository;

import com.rightmeprove.airbnb.airBnbApp.entity.Hotel;
import com.rightmeprove.airbnb.airBnbApp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Hotel entity.
 * Provides CRUD operations and custom queries for Hotel.
 */
@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {

    /**
     * Finds all hotels owned by a specific user (hotel manager).
     * Useful for admin dashboards or managing a user's hotels.
     */
    List<Hotel> findByOwner(User user);
}
