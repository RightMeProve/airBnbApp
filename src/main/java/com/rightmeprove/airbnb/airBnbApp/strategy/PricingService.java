package com.rightmeprove.airbnb.airBnbApp.strategy;

import com.rightmeprove.airbnb.airBnbApp.entity.Inventory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Service responsible for calculating dynamic room pricing.
 *
 * Uses the Strategy + Decorator pattern:
 * - Starts with a BasePricingStrategy.
 * - Wraps it with multiple pricing strategies (surge, occupancy, urgency, holiday).
 * - The final result is a price that reflects all rules combined.
 */
@Service
public class PricingService {

    /**
     * Calculate dynamic price of a room inventory by applying
     * all pricing strategies in sequence.
     *
     * @param inventory The inventory record (room availability, occupancy, surge factor).
     * @return final dynamically calculated price
     */
    public BigDecimal calculateDynamicPricing(Inventory inventory) {
        // Start with the base strategy (just returns room base price)
        PricingStrategy pricingStrategy = new BasePricingStrategy();

        // Decorate the strategy with additional pricing rules
        pricingStrategy = new SurgePricingStrategy(pricingStrategy);      // apply surge factor if present
        pricingStrategy = new OccupancyPricingStrategy(pricingStrategy);  // increase price if >80% occupancy
        pricingStrategy = new UrgencyPricingStrategy(pricingStrategy);    // increase price if last-minute booking
        pricingStrategy = new HolidayPricingStrategy(pricingStrategy);    // apply holiday markup

        // Calculate final price after applying all strategies
        return pricingStrategy.calculatePrice(inventory);
    }
}
