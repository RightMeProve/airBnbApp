package com.rightmeprove.airbnb.airBnbApp.strategy;

import com.rightmeprove.airbnb.airBnbApp.entity.Inventory;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

/**
 * ⚡ SurgePricingStrategy
 *
 * Decorator strategy that applies a surge multiplier on top of another pricing strategy.
 *
 * Example:
 * - Base price = 100
 * - Inventory.surgeFactor = 1.5
 * - Final price = 150
 *
 * Pattern:
 * - Decorator: wraps another PricingStrategy (Base or previously decorated)
 * - Strategy: interchangeable pricing logic
 */
@RequiredArgsConstructor // generates constructor for final field `wrapped`
public class SurgePricingStrategy implements PricingStrategy {

    // Wrapped strategy → could be BasePricingStrategy or another decorator
    private final PricingStrategy wrapped;

    /**
     * Calculate final price by applying surge factor.
     *
     * @param inventory Inventory record containing base price and surgeFactor
     * @return price after applying surge factor
     */
    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        // Step 1: Get price from wrapped strategy
        BigDecimal price = wrapped.calculatePrice(inventory);

        // Step 2: Multiply by surgeFactor (dynamic multiplier)
        return price.multiply(inventory.getSurgeFactor());
    }
}
