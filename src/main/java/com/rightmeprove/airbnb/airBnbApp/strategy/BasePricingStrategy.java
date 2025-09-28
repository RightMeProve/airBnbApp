package com.rightmeprove.airbnb.airBnbApp.strategy;

import com.rightmeprove.airbnb.airBnbApp.entity.Inventory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Pricing strategy that always returns the room's base price.
 * This acts as the simplest/default strategy (no surge/dynamic pricing applied).
 */
public class BasePricingStrategy implements PricingStrategy {

    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        // Simply return the base price of the associated room
        return inventory.getRoom().getBasePrice();
    }
}
