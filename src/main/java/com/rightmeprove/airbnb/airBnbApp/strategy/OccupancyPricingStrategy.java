package com.rightmeprove.airbnb.airBnbApp.strategy;

import com.rightmeprove.airbnb.airBnbApp.entity.Inventory;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

/**
 * ⚡ OccupancyPricingStrategy
 *
 * A decorator strategy that adjusts room price based on occupancy rate.
 *
 * Pattern used:
 * 1. Strategy Pattern – interchangeable pricing logic.
 * 2. Decorator Pattern – wraps another PricingStrategy to add occupancy-based adjustment.
 *
 * Behavior:
 * - Wraps an existing PricingStrategy (BasePricingStrategy, HolidayPricingStrategy, etc.)
 * - Calculates occupancy rate for a specific room and date
 * - If occupancy exceeds 80%, applies a 20% price surge
 *
 * Example usage:
 *   PricingStrategy base = new BasePricingStrategy();
 *   PricingStrategy occupancy = new OccupancyPricingStrategy(base);
 *   BigDecimal finalPrice = occupancy.calculatePrice(inventory);
 */
@RequiredArgsConstructor // generates constructor for final field `wrapped`
public class OccupancyPricingStrategy implements PricingStrategy {

    // Wrapped pricing strategy (decorated)
    private final PricingStrategy wrapped;

    /**
     * Calculate final room price with occupancy-based surge applied.
     *
     * @param inventory Inventory record for the room and date
     * @return BigDecimal final price after occupancy adjustment
     */
    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        // Step 1: Get base price from wrapped strategy
        BigDecimal price = wrapped.calculatePrice(inventory);

        // Step 2: Calculate occupancy rate (booked rooms / total rooms)
        // ⚠️ Avoid division by zero
        double occupancyRate = inventory.getTotalCount() > 0
                ? (double) inventory.getBookedCount() / inventory.getTotalCount()
                : 0.0;

        // Step 3: Apply surge if occupancy > 80%
        if (occupancyRate > 0.8) {
            price = price.multiply(BigDecimal.valueOf(1.2)); // 20% increase
        }

        // Step 4: Return final adjusted price
        return price;
    }
}
