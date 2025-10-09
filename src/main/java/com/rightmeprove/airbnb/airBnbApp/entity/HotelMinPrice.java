package com.rightmeprove.airbnb.airBnbApp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class HotelMinPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Primary key, auto-incremented
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    // Many min-price records belong to one hotel
    private Hotel hotel;

    @Column(nullable = false)
    // Date for which this minimum price applies
    private LocalDate date;

    @Column(nullable = false, precision = 10, scale = 2)
    // Minimum price of the hotel for that date
    private BigDecimal price;

    @CreationTimestamp
    // Auto-set when record is created
    private LocalDateTime createdAt;

    @UpdateTimestamp
    // Auto-updated whenever record changes
    private LocalDateTime updatedAt;

    // Convenience constructor for hotel and date
    public HotelMinPrice(Hotel hotel, LocalDate date) {
        this.hotel = hotel;
        this.date = date;
    }
}
