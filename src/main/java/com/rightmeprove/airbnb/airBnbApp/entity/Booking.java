package com.rightmeprove.airbnb.airBnbApp.entity;

import com.rightmeprove.airbnb.airBnbApp.entity.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Primary key, auto-incremented
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    // Many bookings belong to one hotel
    private Hotel hotel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    // Many bookings belong to one room type
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    // Many bookings made by one user (customer)
    private User user;

    @Column(nullable = false)
    // Number of rooms booked
    private Integer roomsCount;

    @Column(nullable = false)
    // Check-in date
    private LocalDate checkInDate;

    @Column(nullable = false)
    // Check-out date
    private LocalDate checkOutDate;

    @CreationTimestamp
    // Automatically set when booking is created
    private LocalDateTime createdAt;

    @UpdateTimestamp
    // Automatically updated whenever booking changes
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    // Booking status stored as string (safer than ordinal)
    private BookingStatus bookingStatus;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "booking_guest",
            joinColumns = @JoinColumn(name = "booking_id"),
            inverseJoinColumns = @JoinColumn(name = "guest_id")
    )
    /*
     * Many-to-Many relationship:
     * - A booking can have multiple guests
     * - A guest can belong to multiple bookings
     * - JPA creates a join table "booking_guest" to manage the relationship
     */
    private Set<Guest> guests;

    @Column(nullable = false, precision = 10, scale = 2)
    // Total amount for this booking
    private BigDecimal amount;

    @Column(unique = true)
    // Stripe or payment session ID for tracking payment
    private String paymentSessionId;
}
