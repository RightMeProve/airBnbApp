package com.rightmeprove.airbnb.airBnbApp.entity;

import jakarta.persistence.*;          // JPA annotations
import lombok.*;
import org.hibernate.annotations.CreationTimestamp; // Auto-set creation timestamp
import org.hibernate.annotations.UpdateTimestamp;   // Auto-update timestamp

import java.math.BigDecimal;          // For exact money/decimal calculations
import java.time.LocalDate;           // For inventory date
import java.time.LocalDateTime;       // For timestamps

@Entity
@Getter
@Setter
@Table(
        uniqueConstraints = @UniqueConstraint(
                name = "unique_hotel_room-date",
                columnNames = {"hotel_id", "room_id", "date"}
        )
        /*
         * Ensures there cannot be two rows with the same (hotel_id, room_id, date).
         * Example: Hotel X, Room Y, Date Z → only one entry allowed.
         * Prevents duplicate inventory records for the same room on the same date.
         */
)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Auto-increment primary key
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    /*
     * Many inventory records belong to one Hotel.
     *
     * FetchType.LAZY → means the Hotel is NOT immediately loaded when
     * you fetch Inventory. Instead, a proxy is created.
     * The actual SQL query for Hotel runs ONLY when you call something like:
     *   inventory.getHotel().getName();
     *
     * Why LAZY? → Performance! If you load 10,000 inventory rows, you
     * don’t want Hibernate to also load 10,000 Hotels unnecessarily.
     *
     * Note: By default, @ManyToOne is EAGER, but overriding to LAZY is common.
     */
    @JoinColumn(name = "hotel_id", nullable = false)
    // Foreign key to Hotel table
    private Hotel hotel;

    @ManyToOne(fetch = FetchType.LAZY)
    /*
     * Many inventory records belong to one Room.
     *
     * Works the same way as above: Room is lazily fetched,
     * so the query for Room only runs when you actually access it.
     */
    @JoinColumn(name = "room_id", nullable = false)
    // Foreign key to Room table
    private Room room;

    @Column(nullable = false)
    // Date of availability (inventory is tracked per day)
    private LocalDate date;

    @Column(nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    // How many rooms already booked for this date (default = 0)
    private int bookedCount;

    @Column(nullable = false,columnDefinition = "INTEGER DEFAULT 0")
    private Integer reservedCount;

    @Column(nullable = false)
    // Total rooms available for booking on this date
    private Integer totalCount;

    @Column(nullable = false, precision = 5, scale = 2)
    /*
     * Surge factor (e.g., 1.00 = normal price, 1.50 = 50% surge).
     * precision = 5 → up to 5 digits total
     * scale = 2 → 2 digits after decimal
     * Example values: 1.00, 2.50, 10.00
     * In DB → NUMERIC(5,2)
     */
    private BigDecimal surgeFactor;

    @Column(nullable = false, precision = 10, scale = 2)
    /*
     * Final price for this room on the given date (after applying surge factor etc.).
     * precision = 10, scale = 2 → e.g., 99999999.99
     * In DB → NUMERIC(10,2)
     */
    private BigDecimal price;

    @Column(nullable = false)
    // City (duplicate for quick search without joining Hotel table)
    private String city;

    @Column(nullable = false)
    // If true → booking is closed for this date (e.g., maintenance, sold out)
    private Boolean closed;

    @CreationTimestamp
    // Auto-set when inventory record is created
    private LocalDateTime createdAt;

    @UpdateTimestamp
    // Auto-updated whenever the record changes
    private LocalDate updatedAt;
}
