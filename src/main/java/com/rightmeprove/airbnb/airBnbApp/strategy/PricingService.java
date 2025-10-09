package com.rightmeprove.airbnb.airBnbApp.strategy;

import com.rightmeprove.airbnb.airBnbApp.entity.Inventory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * ⚡ PricingService
 *
 * Responsible for calculating dynamic room pricing using a chain of strategies.
 *
 * Pattern:
 * 1. Strategy Pattern – interchangeable pricing logic (BasePricingStrategy, SurgePricingStrategy, etc.).
 * 2. Decorator Pattern – chains multiple strategies on top of each other.
 *
 * Usage:
 * - Start with BasePricingStrategy (room base price).
 * - Wrap with Surge, Occupancy, Urgency, Holiday strategies.
 * - Each strategy adjusts price according to specific rules.
 */
@Service
public class PricingService {

    /**
     * Calculates the final price of a single room inventory record
     * after applying all pricing strategies in sequence.
     *
     * @param inventory Inventory record containing room, availability, occupancy, etc.
     * @return final price after all dynamic pricing rules
     */
    public BigDecimal calculateDynamicPricing(Inventory inventory) {
        // Step 1: Start with base price
        PricingStrategy pricingStrategy = new BasePricingStrategy();

        // Step 2: Apply all dynamic pricing decorators in order
        pricingStrategy = new SurgePricingStrategy(pricingStrategy);      // add surge factor if present
        pricingStrategy = new OccupancyPricingStrategy(pricingStrategy);  // increase price if occupancy > 80%
        pricingStrategy = new UrgencyPricingStrategy(pricingStrategy);    // increase price if last-minute
        pricingStrategy = new HolidayPricingStrategy(pricingStrategy);    // holiday markup

        // Step 3: Calculate final price
        return pricingStrategy.calculatePrice(inventory);
    }

    /**
     * Calculates the total price for a list of inventories (e.g., multiple nights).
     *
     * @param inventoryList list of inventory records
     * @return sum of dynamically calculated prices
     */
    public BigDecimal calculateTotalPrice(List<Inventory> inventoryList){
        return inventoryList.stream()
                .map(this::calculateDynamicPricing) // calculate dynamic price for each night
                .reduce(BigDecimal.ZERO, BigDecimal::add); // sum all prices
    }
}
