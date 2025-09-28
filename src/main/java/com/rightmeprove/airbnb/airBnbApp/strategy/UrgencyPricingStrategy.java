package com.rightmeprove.airbnb.airBnbApp.strategy;

import com.rightmeprove.airbnb.airBnbApp.entity.Inventory;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Decorator strategy that increases price for urgent (last-minute) bookings.
 *
 * - Wraps another PricingStrategy (e.g., BasePricingStrategy).
 * - If the booking date is within the next 7 days (including today),
 *   applies a 15% markup.
 */
@RequiredArgsConstructor // generates constructor for final field `wrapped`
public class UrgencyPricingStrategy implements PricingStrategy {

    // Wrapped strategy (can be base or another decorated strategy)
    private final PricingStrategy wrapped;

    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        // Start with the price from the wrapped strategy
        BigDecimal price = wrapped.calculatePrice(inventory);

        LocalDate today = LocalDate.now();

        // Apply urgency markup if the inventory date is:
        // - today or later (not in the past)
        // - AND within the next 7 days
        if (!inventory.getDate().isBefore(today) && inventory.getDate().isBefore(today.plusDays(7))) {
            price = price.multiply(BigDecimal.valueOf(1.15)); // +15%
        }

        return price;
    }
}
