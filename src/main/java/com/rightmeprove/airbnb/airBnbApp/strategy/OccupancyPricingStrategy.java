package com.rightmeprove.airbnb.airBnbApp.strategy;

import com.rightmeprove.airbnb.airBnbApp.entity.Inventory;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

/**
 * Decorator strategy that applies dynamic pricing
 * based on occupancy rate of a room.
 *
 * - Wraps another PricingStrategy (e.g., BasePricingStrategy).
 * - If occupancy rate > 80%, increases price by 20%.
 */
@RequiredArgsConstructor // generates constructor for final field `wrapped`
public class OccupancyPricingStrategy implements PricingStrategy {

    // Wrapped strategy → base or previously decorated strategy
    private final PricingStrategy wrapped;

    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        // Get price from the wrapped strategy
        BigDecimal price = wrapped.calculatePrice(inventory);

        // Calculate occupancy rate = booked rooms / total rooms
        double occupancyRate = (double) inventory.getBookedCount() / inventory.getTotalCount();

        // If occupancy > 80%, apply 20% surge
        if (occupancyRate > 0.8) {
            price = price.multiply(BigDecimal.valueOf(1.2));
        }

        return price;
    }
}
