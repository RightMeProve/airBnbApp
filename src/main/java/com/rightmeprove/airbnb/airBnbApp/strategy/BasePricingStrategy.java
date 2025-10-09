package com.rightmeprove.airbnb.airBnbApp.strategy;

import com.rightmeprove.airbnb.airBnbApp.entity.Inventory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * ⚡ BasePricingStrategy
 *
 * This is the simplest pricing strategy for hotel rooms.
 * It does not apply any dynamic pricing, surge, or discounts.
 *
 * Responsibilities:
 * 1. Always return the room's base price.
 * 2. Acts as the default strategy in a chain of pricing strategies.
 *
 * Example usage:
 * - If no other strategies (like surge pricing or seasonal adjustments) are applied,
 *   this ensures the system always has a fallback price.
 */
public class BasePricingStrategy implements PricingStrategy {

    /**
     * Calculates the price of a room for the given inventory entry.
     *
     * @param inventory The inventory record for a specific room and date
     * @return BigDecimal representing the room's base price
     */
    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        // Simply return the base price of the associated room
        return inventory.getRoom().getBasePrice();
    }
}
