package com.rightmeprove.airbnb.airBnbApp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "hotel") // Maps entity to "hotel" table
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Primary key, auto-incremented
    private Long id;

    @Column(nullable = false)
    private String name; // Hotel name

    private String city; // Hotel city

    @Column(columnDefinition = "TEXT[]")
    private String[] photos; // URLs of hotel photos

    @Column(columnDefinition = "TEXT[]")
    private String[] amenities; // Amenities like WiFi, AC, Pool, etc.

    @CreationTimestamp
    private LocalDateTime createdAt; // Auto-set when hotel record is created

    @UpdateTimestamp
    private LocalDateTime updatedAt; // Auto-updated whenever hotel record changes

    @Embedded
    // Embedded contact info (address, phone, email, location) stored in same table
    private HotelContactInfo contactInfo;

    @Column(nullable = false)
    private Boolean active; // Whether the hotel is listed/active

    @OneToMany(mappedBy = "hotel", fetch = FetchType.LAZY)
    /*
     * One hotel can have multiple rooms.
     * Inverse side of relationship; "hotel" field in Room owns it.
     * Lazy loading prevents loading all rooms unless accessed.
     */
    @JsonIgnore // Prevent infinite recursion during JSON serialization
    private List<Room> rooms;

    @ManyToOne(optional = false)
    // Owner of the hotel (usually a HOTEL_MANAGER)
    private User owner;
}
