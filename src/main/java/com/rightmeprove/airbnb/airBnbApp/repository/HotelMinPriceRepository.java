package com.rightmeprove.airbnb.airBnbApp.repository;

import com.rightmeprove.airbnb.airBnbApp.dto.HotelPriceDto;
import com.rightmeprove.airbnb.airBnbApp.entity.Hotel;
import com.rightmeprove.airbnb.airBnbApp.entity.HotelMinPrice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Repository for HotelMinPrice entity.
 *
 * - Stores the minimum daily price of hotels for fast search and filtering.
 * - Supports custom queries for retrieving hotels by city and date range.
 */
public interface HotelMinPriceRepository extends JpaRepository<HotelMinPrice, Long> {

    /**
     * Finds hotels with available inventory in a given city and date range.
     * Returns a page of HotelPriceDto with hotel info + average price.
     *
     * Filters:
     * - city
     * - date range
     * - active hotels only
     * Groups results by hotel and calculates average price over the period.
     */
    @Query("""
           SELECT new com.rightmeprove.airbnb.airBnbApp.dto.HotelPriceDto(i.hotel, AVG(i.price))
           FROM HotelMinPrice i
           WHERE i.hotel.city = :city
             AND i.date BETWEEN :startDate AND :endDate
             AND i.hotel.active = true
           GROUP BY i.hotel
           """)
    Page<HotelPriceDto> findHotelWithAvailableInventory(
            @Param("city") String city,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomsCount") Integer roomsCount, // currently unused
            @Param("dateCount") Long dateCount,      // currently unused
            Pageable pageable
    );

    /**
     * Find a HotelMinPrice entry for a specific hotel and date.
     * Useful for updating or retrieving the minimum price for a given day.
     */
    Optional<HotelMinPrice> findByHotelAndDate(Hotel hotel, LocalDate date);
}
