package com.rightmeprove.airbnb.airBnbApp.strategy;

import com.rightmeprove.airbnb.airBnbApp.entity.Inventory;

import java.math.BigDecimal;

/**
 * Strategy interface for dynamic room pricing.
 *
 * Implementations define different ways to calculate price
 * based on various factors (base price, occupancy, holiday, surge, etc.).
 *
 * This follows the Strategy design pattern:
 * - Each strategy implements the same interface.
 * - The PricingService can apply them interchangeably or in combination.
 */
public interface PricingStrategy {

    /**
     * Calculate the final price for the given inventory.
     *
     * @param inventory The room inventory (contains base price, availability, surge factor, etc.)
     * @return The calculated price after applying strategy-specific rules
     */
    BigDecimal calculatePrice(Inventory inventory);
}
