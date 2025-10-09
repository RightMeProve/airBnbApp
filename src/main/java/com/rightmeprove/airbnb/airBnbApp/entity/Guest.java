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
    // Each guest can be linked to a user account (optional)
    private User user;

    @Column(nullable = false)
    private String name; // Guest's full name

    private Integer age; // Guest's age

    @Enumerated(EnumType.STRING)
    private Gender gender; // Stored as string (e.g., "MALE", "FEMALE", "OTHER")

    @CreationTimestamp
    private LocalDateTime createdAt; // Auto-set when record is created

    /*
     * Many-to-Many with Booking (inverse side):
     * - A guest can belong to multiple bookings.
     * - A booking can include multiple guests.
     * - No @JoinTable here because Booking is the owning side.
     * - JPA uses `mappedBy="guests"` in Booking to manage the join table `booking_guest`.
     * - Prevents redundant join tables and keeps relationships consistent.
     */
}
