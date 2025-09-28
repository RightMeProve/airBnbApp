package com.rightmeprove.airbnb.airBnbApp.repository;

import com.rightmeprove.airbnb.airBnbApp.entity.Hotel;
import com.rightmeprove.airbnb.airBnbApp.entity.Inventory;
import com.rightmeprove.airbnb.airBnbApp.entity.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    /**
     * Delete all inventory records for a specific room.
     * Useful when a room is deleted → ensures orphan inventories are cleaned up.
     */
    void deleteByRoom(Room room);

    /**
     * Finds hotels with available inventory that match the search criteria.
     *
     * - Filters by city.
     * - Checks if inventory exists for *every single date* in the requested range.
     * - Ensures room is not closed.
     * - Ensures available count >= requested rooms (total - booked - reserved).
     *
     * GROUP BY i.hotel, i.room
     * - Ensures availability is checked per hotel-room combination.
     *
     * HAVING COUNT(i.date) = :dateCount
     * - Makes sure that *all requested days* are available.
     *   Example: if date range = 3 nights, we must have inventory rows for all 3 dates.
     *
     * DISTINCT i.hotel
     * - Ensures the same hotel is not returned multiple times.
     *
     * Pageable → supports pagination for search results.
     */
    @Query("""
            SELECT DISTINCT i.hotel
            FROM Inventory i
            WHERE i.city = :city
              AND i.date BETWEEN :startDate AND :endDate
              AND i.closed = false
              AND (i.totalCount - i.bookedCount - i.reservedCount) >= :roomsCount
            GROUP BY i.hotel, i.room
            HAVING COUNT(i.date) = :dateCount
            """)
    Page<Hotel> findHotelsWithAvailableInventory(
            @Param("city") String city,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomsCount") Integer roomsCount,
            @Param("dateCount") Long dateCount,
            Pageable pageable
    );

    /**
     * Locks inventory rows (per room + date range) so no two bookings
     * can reserve the same rooms concurrently.
     *
     * @Lock(PESSIMISTIC_WRITE)
     * - Adds a "SELECT ... FOR UPDATE" at DB level.
     * - Ensures only one transaction can modify those rows at a time.
     *
     * Conditions:
     * - Room ID must match.
     * - Dates must fall within check-in/check-out range.
     * - Inventory must not be closed.
     * - Must have enough available count left.
     *
     * Used during booking initialization step before confirming reservations.
     */
    @Query("""
            SELECT i
            FROM Inventory i
            WHERE i.room.id = :roomId
              AND i.date BETWEEN :startDate AND :endDate
              AND i.closed = false
              AND (i.totalCount - i.bookedCount - i.reservedCount) >= :roomsCount
            """)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Inventory> findAndLockAvailableInventory(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomsCount") Integer roomsCount
    );

    List<Inventory> findByHotelAndDateBetween(Hotel hotel, LocalDate startDate, LocalDate endDate);
}
