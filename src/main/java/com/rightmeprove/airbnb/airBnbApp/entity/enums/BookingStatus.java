package com.rightmeprove.airbnb.airBnbApp.entity.enums;

/**
 * Enum representing the various stages of a booking lifecycle.
 */
public enum BookingStatus {
    RESERVED,        // Booking created but no guests added yet
    GUESTS_ADDED,    // Guests have been added to the booking
    PAYMENT_PENDING, // Booking awaits payment completion
    CONFIRMED,       // Payment completed, booking confirmed
    CANCELLED,       // Booking cancelled by user or admin
    EXPIRED          // Booking expired due to timeout or non-payment
}
