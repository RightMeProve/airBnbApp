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
 * - HotelMinPrice stores the minimum daily price of a hotel (for fast search/filtering).
 * - Provides custom queries for searching hotels by city and date range.
 */
public interface HotelMinPriceRepository extends JpaRepository<HotelMinPrice, Long> {

    /**
     * Finds hotels with available inventory in a given city and date range.
     *
     * - Returns a page of HotelPriceDto (hotel + average price).
     * - Filters by:
     *   - city
     *   - date range (between startDate and endDate)
     *   - active hotels only
     * - Groups results by hotel and computes AVG price over the period.
     *
     * @param city city name
     * @param startDate start of date range
     * @param endDate end of date range
     * @param roomsCount (currently unused in query, could be used for filtering later)
     * @param dateCount (currently unused in query, could be used for filtering later)
     * @param pageable pagination info
     * @return paginated HotelPriceDto results
     */
    @Query("""
           SELECT new com.rightmeprove.airbnb.airBnbApp.dto.HotelPriceDto(i.hotel, AVG(i.price))
           FROM HotelMinPrice i
           WHERE i.hotel.city = :city
             AND i.date BETWEEN :startDate AND :endDate
             AND i.hotel.active = true
           GROUP BY i.hotel
           """
    )
    Page<HotelPriceDto> findHotelWithAvailableInventory(
            @Param("city") String city,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomsCount") Integer roomsCount, // TODO: not used in query, consider removing or implementing
            @Param("dateCount") Long dateCount,      // TODO: not used in query, consider removing or implementing
            Pageable pageable
    );

    /**
     * Find a HotelMinPrice entry for a specific hotel and date.
     * Useful when updating or retrieving the min price for a given day.
     */
    Optional<HotelMinPrice> findByHotelAndDate(Hotel hotel, LocalDate date);
}
