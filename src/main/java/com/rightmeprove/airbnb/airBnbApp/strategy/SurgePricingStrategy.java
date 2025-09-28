package com.rightmeprove.airbnb.airBnbApp.strategy;

import com.rightmeprove.airbnb.airBnbApp.entity.Inventory;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

/**
 * Decorator strategy that applies surge pricing
 * based on the surgeFactor stored in Inventory.
 *
 * Example:
 * - Base price = 100
 * - surgeFactor = 1.5
 * - Final price = 150
 */
@RequiredArgsConstructor // generates constructor for the final field `wrapped`
public class SurgePricingStrategy implements PricingStrategy {

    // Wrapped strategy → can be BasePricingStrategy or another decorated strategy
    private final PricingStrategy wrapped;

    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        // Start with price from wrapped strategy
        BigDecimal price = wrapped.calculatePrice(inventory);

        // Apply surge multiplier (comes from Inventory.surgeFactor)
        return price.multiply(inventory.getSurgeFactor());
    }
}
