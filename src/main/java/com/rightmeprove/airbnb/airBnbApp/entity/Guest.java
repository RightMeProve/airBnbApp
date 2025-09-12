package com.rightmeprove.airbnb.airBnbApp.entity;

import com.rightmeprove.airbnb.airBnbApp.entity.enums.Gender;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Getter
@Setter
public class Guest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Primary key, auto-incremented
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    // Each guest is linked to one user account (e.g., the main account holder)
    private User user;

    @Column(nullable = false)
    private String name; // Guest's full name

    private Integer age; // Guest's age

    @Enumerated(EnumType.STRING)
    private Gender gender; // Enum stored as string (e.g., "MALE", "FEMALE")

    @CreationTimestamp
    private LocalDateTime createdAt; // Auto-filled when record is created

    /*
     * Many-to-Many relationship with Booking.
     * - A guest can appear in multiple bookings.
     * - A booking can have multiple guests.
     *
     * IMPORTANT:
     * - We do NOT define @JoinTable here, because Booking is already the "owning side".
     * - Instead, we use mappedBy = "guests" to tell JPA:
     *   "Look at the 'guests' field in Booking for the join table mapping."
     *
     * This way:
     * - Only ONE join table (`booking_guest`) is created.
     * - Both Booking and Guest entities can navigate the relationship.
     *
     * ❌ If you define @JoinTable on BOTH sides:
     *    → Hibernate will create TWO join tables (booking_guest and guest_booking),
     *      which causes redundant and inconsistent mappings.
     */
}
