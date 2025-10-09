package com.rightmeprove.airbnb.airBnbApp.strategy;

import com.rightmeprove.airbnb.airBnbApp.entity.Inventory;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

/**
 * ⚡ HolidayPricingStrategy
 *
 * A decorator strategy that adds a holiday markup to an existing pricing strategy.
 *
 * Pattern used:
 * 1. Strategy Pattern – allows interchangeable pricing logic.
 * 2. Decorator Pattern – wraps another PricingStrategy to add extra behavior (holiday markup)
 *
 * Responsibilities:
 * - Delegates initial price calculation to the wrapped strategy (could be BasePricingStrategy or SurgePricingStrategy).
 * - Applies an additional markup if the date is a holiday.
 *
 * Example usage:
 *   PricingStrategy base = new BasePricingStrategy();
 *   PricingStrategy holiday = new HolidayPricingStrategy(base);
 *   BigDecimal finalPrice = holiday.calculatePrice(inventory);
 */
@RequiredArgsConstructor // generates constructor for final field `wrapped`
public class HolidayPricingStrategy implements PricingStrategy {

    // The wrapped PricingStrategy. Can be BasePricingStrategy, SurgePricingStrategy, etc.
    private final PricingStrategy wrapped;

    /**
     * Calculate the final room price, including holiday markup if applicable.
     *
     * @param inventory The inventory record for a specific room and date
     * @return BigDecimal representing the final calculated price
     */
    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        // Step 1: Delegate to the wrapped strategy for base price
        BigDecimal price = wrapped.calculatePrice(inventory);

        // Step 2: Check if today is a holiday
        // TODO: Replace hardcoded 'true' with real holiday-checking logic (API, DB, config, etc.)
        boolean isTodayHoliday = true;

        // Step 3: Apply 25% markup if it’s a holiday
        if (isTodayHoliday) {
            price = price.multiply(BigDecimal.valueOf(1.25));
        }

        // Step 4: Return the final price
        return price;
    }
}
