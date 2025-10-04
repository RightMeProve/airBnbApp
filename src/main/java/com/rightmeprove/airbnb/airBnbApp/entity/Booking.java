package com.rightmeprove.airbnb.airBnbApp.entity;

import com.rightmeprove.airbnb.airBnbApp.entity.enums.BookingStatus; // Enum for status (CONFIRMED, CANCELLED, etc.)
import jakarta.persistence.*;          // JPA annotations
import lombok.*;
import org.hibernate.annotations.CreationTimestamp; // Auto-set creation time
import org.hibernate.annotations.UpdateTimestamp;   // Auto-update time

import java.math.BigDecimal;
import java.time.LocalDate;           // For check-in/check-out
import java.time.LocalDateTime;       // For timestamps
import java.util.Set;                 // For storing multiple guests

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Primary key, auto-increment
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    /*
     * Many bookings can belong to one hotel.
     * LAZY = Hotel is only loaded when accessed (performance-friendly).
     */
    @JoinColumn(name = "hotel_id", nullable = false)
    // Foreign key column "hotel_id"
    private Hotel hotel;

    @ManyToOne(fetch = FetchType.LAZY)
    /*
     * Many bookings can belong to one room type.
     * Example: 5 Deluxe rooms booked from Hotel A.
     */
    @JoinColumn(name = "room_id", nullable = false)
    // Foreign key column "room_id"
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    /*
     * Many bookings can be made by one user (customer).
     */
    @JoinColumn(name = "user_id", nullable = false)
    // Foreign key column "user_id"
    private User user;

    @Column(nullable = false)
    // How many rooms of this type were booked in this reservation
    private Integer roomsCount;

    @Column(nullable = false)
    // Check-in date
    private LocalDate checkInDate;

    @Column(nullable = false)
    // Check-out date
    private LocalDate checkOutDate;

    @CreationTimestamp
    // Auto-set when booking is created
    private LocalDateTime createdAt;

    @UpdateTimestamp
    // Auto-updated when booking is modified
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    /*
     * Stores enum values (e.g., CONFIRMED, CANCELLED, PENDING) as Strings in DB.
     * Using STRING is better than ORDINAL (numbers) → safer if enum order changes.
     */
    @Column(nullable = false)
    private BookingStatus bookingStatus;

    @ManyToMany(fetch = FetchType.LAZY)
    /*
     * Many-to-Many relationship:
     * - A booking can have multiple guests.
     * - A guest can belong to multiple bookings (e.g., same guest booked multiple times).
     *
     * JPA handles this via a "join table" because relational databases don’t allow
     * direct many-to-many relationships. Instead, an extra table is created to map them.
     */
    @JoinTable(
            name = "booking_guest",
            /*
             * The name of the join table in the database.
             * This table will hold pairs of IDs (booking_id, guest_id)
             * linking which guest belongs to which booking.
             */

            joinColumns = @JoinColumn(name = "booking_id"),
            /*
             * `joinColumns` → refers to the current entity (Booking).
             * - "booking_id" is the foreign key column in the join table
             *   that points back to the `Booking` table's primary key (id).
             * - If not specified, JPA would default to something like "booking_id".
             */

            inverseJoinColumns = @JoinColumn(name = "guest_id")
            /*
             * `inverseJoinColumns` → refers to the other entity (Guest).
             * - "guest_id" is the foreign key column in the join table
             *   that points back to the `Guest` table's primary key (id).
             * - This defines the "other side" of the relationship.
             */
    )
    private Set<Guest> guests;

    @Column(nullable = false,precision = 10,scale = 2)
    private BigDecimal amount;

    @Column(unique = true)
    private String paymentSessionId;
}
