package com.rightmeprove.airbnb.airBnbApp.entity;

import jakarta.persistence.*;          // JPA annotations for ORM mapping
import lombok.Getter;                 // Lombok generates getter methods
import lombok.Setter;                 // Lombok generates setter methods
import org.hibernate.annotations.CreationTimestamp; // Auto-set creation time
import org.hibernate.annotations.UpdateTimestamp;   // Auto-set update time

import java.math.BigDecimal;          // Used for prices (better than double for money)
import java.time.LocalDateTime;       // Timestamp class

@Entity
@Getter
@Setter
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Auto-increment primary key (Room ID)
    private Long id;

    @ManyToOne  // Many rooms can belong to one hotel
    @JoinColumn(name = "hotel_id", nullable = false)
    // Foreign key column "hotel_id" links Room -> Hotel
    private Hotel hotel;

    @Column(nullable = false)
    // Type of room (e.g., "Deluxe", "Suite", "Standard")
    private String type;

    @Column(nullable = false, precision = 10, scale = 2)
    /*
     * BigDecimal is used instead of double/float because:
     * - double/float use binary fractions -> can cause rounding errors (e.g. 999.99 may store as 999.9899999)
     * - BigDecimal stores exact decimal values -> critical for prices & money
     *
     * precision = 10 -> total digits allowed (before + after decimal)
     * scale = 2 -> digits after decimal
     * Example: 99999999.99 (8 digits before + 2 after = 10 total) ✅
     *          1000000000.00 (11 digits) ❌ exceeds precision
     *
     * In DB this becomes NUMERIC(10,2)
     */
    private BigDecimal basePrice;

    @Column(columnDefinition = "TEXT[]")
    // Array of photo URLs for this room
    private String[] photos;

    @Column(columnDefinition = "TEXT[]")
    // Room-specific amenities (AC, TV, etc.)
    private String[] amenities;

    @Column(nullable = false)
    // Total number of rooms of this type in the hotel
    private Integer totalCount;

    @Column(nullable = false)
    // How many people this room can accommodate
    private Integer capacity;

    @CreationTimestamp
    @Column(updatable = false)
    // Auto-set when created (can’t be updated later)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    // Auto-updated whenever the room is modified
    private LocalDateTime updatedAt;
}
