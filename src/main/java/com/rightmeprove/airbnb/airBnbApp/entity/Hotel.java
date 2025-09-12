package com.rightmeprove.airbnb.airBnbApp.entity;

import jakarta.persistence.*;  // JPA annotations for entity mapping
import lombok.Getter;        // Lombok generates getters automatically
import lombok.Setter;        // Lombok generates setters automatically
import org.hibernate.annotations.CreationTimestamp; // Auto-fill created time
import org.hibernate.annotations.UpdateTimestamp;   // Auto-fill updated time

import java.time.LocalDateTime; // Date-time class for timestamps
import java.util.List;

// Marks this class as a JPA entity (maps to a database table)
@Entity
@Getter  // Lombok generates getter methods for all fields
@Setter  // Lombok generates setter methods for all fields
@Table(name = "hotel")  // Explicitly maps this entity to the "hotel" table in DB
public class Hotel {

    @Id  // Marks this field as primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Auto-increment primary key (DB handles ID generation, usually SERIAL in Postgres/MySQL)
    private Long id;

    @Column(nullable = false)
    // Column cannot be NULL in DB
    private String name;

    private String city;  // City where hotel is located

    @Column(columnDefinition = "TEXT[]")
    // Array of photo URLs, stored as Postgres TEXT[]
    private String[] photos;

    @Column(columnDefinition = "TEXT[]")
    // Array of amenities (like WiFi, AC, Pool, etc.)
    private String[] amenities;

    @CreationTimestamp
    // Auto-filled when entity is first persisted
    private LocalDateTime createdAt;

    @UpdateTimestamp
    // Auto-updated when entity is modified
    private LocalDateTime updatedAt;

    @Embedded
    /*
     * Embeds fields from HotelContactInfo directly into this table.
     * Example: columns become "address", "phone_number", "email", "location".
     * No separate table is created for HotelContactInfo.
     */
    private HotelContactInfo contactInfo;

    @Column(nullable = false)
    private Boolean active; // Whether the hotel is currently active/listed

    @OneToMany(mappedBy = "hotel", fetch = FetchType.LAZY)
    /*
     * One Hotel → Many Rooms.
     * `mappedBy = "hotel"` → the "hotel" field in Room owns the relationship.
     * This side is inverse; no join column is created here.
     *
     * FetchType.LAZY → Rooms are loaded only when accessed (efficient).
     * If EAGER was used, all rooms would load whenever a Hotel is fetched,
     * which can cause performance issues with large datasets.
     */
    private List<Room> rooms;
}
