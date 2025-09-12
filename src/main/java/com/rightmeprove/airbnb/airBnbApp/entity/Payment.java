package com.rightmeprove.airbnb.airBnbApp.entity;

import com.rightmeprove.airbnb.airBnbApp.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Primary key, auto-incremented by DB
    private Long id;

    @Column(unique = true, nullable = false)
    // Unique transaction ID (e.g., from Razorpay, Stripe, PayPal)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    // Stores enum as String (e.g., "PENDING", "SUCCESS", "FAILED")
    private PaymentStatus paymentStatus;

    @Column(nullable = false, precision = 10, scale = 2)
    /*
     * Stores payment amount.
     * - BigDecimal ensures accurate currency handling (avoids floating-point errors).
     * - precision = 10 → up to 10 digits total.
     * - scale = 2 → 2 digits after the decimal.
     * Example: max value = 99,999,999.99
     */
    private BigDecimal amount;
}
