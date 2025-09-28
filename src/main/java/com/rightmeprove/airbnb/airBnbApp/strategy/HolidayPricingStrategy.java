package com.rightmeprove.airbnb.airBnbApp.strategy;

import com.rightmeprove.airbnb.airBnbApp.entity.Inventory;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

/**
 * Decorator strategy that applies a holiday markup on top of
 * an existing pricing strategy.
 *
 * Uses the Strategy + Decorator pattern:
 * - Wraps another PricingStrategy (e.g., BasePricingStrategy).
 * - Adds extra logic: increases price if today is a holiday.
 */
@RequiredArgsConstructor // generates constructor for final field `wrapped`
public class HolidayPricingStrategy implements PricingStrategy {

    // Wrapped strategy (could be BasePricingStrategy, SurgePricingStrategy, etc.)
    private final PricingStrategy wrapped;

    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        // Get the base price (or price from wrapped strategy)
        BigDecimal price = wrapped.calculatePrice(inventory);

        // TODO: Replace with a real holiday-checking service (API or DB)
        boolean isTodayHoliday = true;

        // Apply a 25% markup if today is a holiday
        if (isTodayHoliday) {
            price = price.multiply(BigDecimal.valueOf(1.25));
        }

        return price;
    }
}
