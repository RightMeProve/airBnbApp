package com.rightmeprove.airbnb.airBnbApp.strategy;

import com.rightmeprove.airbnb.airBnbApp.entity.Inventory;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * ⚡ UrgencyPricingStrategy
 *
 * Decorator strategy that increases price for last-minute bookings.
 *
 * Rules:
 * - Wraps another PricingStrategy (Base or any other decorator).
 * - If the booking date is within the next 7 days (including today),
 *   applies a 15% markup.
 *
 * Pattern:
 * - Decorator: wraps another PricingStrategy.
 * - Strategy: interchangeable pricing logic.
 */
@RequiredArgsConstructor
public class UrgencyPricingStrategy implements PricingStrategy {

    // Wrapped strategy (can be BasePricingStrategy or another decorator)
    private final PricingStrategy wrapped;

    /**
     * Calculate final price after applying urgency markup.
     *
     * @param inventory Inventory record (room, date, etc.)
     * @return dynamically calculated price
     */
    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        // Step 1: Get price from the wrapped strategy
        BigDecimal price = wrapped.calculatePrice(inventory);

        LocalDate today = LocalDate.now();

        // Step 2: Apply 15% urgency markup if booking date is within the next 7 days
        if (!inventory.getDate().isBefore(today) && inventory.getDate().isBefore(today.plusDays(7))) {
            price = price.multiply(BigDecimal.valueOf(1.15)); // +15%
        }

        return price;
    }
}
