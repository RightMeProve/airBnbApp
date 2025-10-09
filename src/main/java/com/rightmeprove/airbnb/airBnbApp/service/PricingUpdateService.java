package com.rightmeprove.airbnb.airBnbApp.service;

import com.rightmeprove.airbnb.airBnbApp.entity.Hotel;
import com.rightmeprove.airbnb.airBnbApp.entity.HotelMinPrice;
import com.rightmeprove.airbnb.airBnbApp.entity.Inventory;
import com.rightmeprove.airbnb.airBnbApp.repository.HotelMinPriceRepository;
import com.rightmeprove.airbnb.airBnbApp.repository.HotelRepository;
import com.rightmeprove.airbnb.airBnbApp.repository.InventoryRepository;
import com.rightmeprove.airbnb.airBnbApp.strategy.PricingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service that handles dynamic pricing updates for all hotels.
 *
 * Responsibilities:
 * 1. Update inventory prices for all rooms using PricingService.
 * 2. Update HotelMinPrice table for fast retrieval of minimum daily hotel prices.
 * 3. Runs automatically every hour using @Scheduled annotation.
 */
@Service
@RequiredArgsConstructor // generates constructor for all final dependencies (dependency injection)
@Slf4j // enables log.info/debug/error
@Transactional // ensures DB operations are atomic
public class PricingUpdateService {

    // Repository to fetch hotels from DB
    private final HotelRepository hotelRepository;

    // Repository to fetch and update inventory prices
    private final InventoryRepository inventoryRepository;

    // Repository to update minimum daily hotel price
    private final HotelMinPriceRepository hotelMinPriceRepository;

    // Strategy service to calculate dynamic pricing for inventory
    private final PricingService pricingService;

    /**
     * Scheduled method that runs at the top of every hour.
     * Updates all hotels’ inventory and min price in batches to avoid memory issues.
     */
    @Scheduled(cron = "0 0 * * * *")
    public void updatePrices() {
        int page = 0;
        int batchSize = 100; // batch size for pagination

        while (true) {
            // Fetch a page of hotels from the DB
            Page<Hotel> hotelPage = hotelRepository.findAll(PageRequest.of(page, batchSize));

            // Stop if no more hotels
            if (hotelPage.isEmpty()) {
                break;
            }

            // Update pricing for each hotel in the current batch
            hotelPage.getContent().forEach(this::updateHotelPrices);

            // Move to the next page
            page++;
        }
    }

    /**
     * Updates inventory prices and minimum daily hotel prices for a single hotel.
     *
     * @param hotel The hotel to update
     */
    private void updateHotelPrices(Hotel hotel) {
        log.info("Updating hotel prices for hotel ID: {}", hotel.getId());

        LocalDate startDate = LocalDate.now();            // start from today
        LocalDate endDate = LocalDate.now().plusYears(1); // update for next 1 year

        // Fetch all inventory entries for this hotel between startDate and endDate
        List<Inventory> inventoryList = inventoryRepository.findByHotelAndDateBetween(hotel, startDate, endDate);

        // Apply dynamic pricing strategies to each inventory
        updateInventoryPrices(inventoryList);

        // Update HotelMinPrice table with minimum price per day
        updateHotelMinPrice(hotel, inventoryList, startDate, endDate);
    }

    /**
     * Updates the HotelMinPrice table with the lowest price per day
     * across all rooms in the hotel.
     *
     * @param hotel         Hotel to update
     * @param inventoryList List of inventory records for the hotel
     * @param startDate     Start date for pricing update
     * @param endDate       End date for pricing update
     */
    private void updateHotelMinPrice(Hotel hotel, List<Inventory> inventoryList,
                                     LocalDate startDate, LocalDate endDate) {

        // Compute minimum price for each date across all rooms
        Map<LocalDate, BigDecimal> dailyMinPrices = inventoryList.stream()
                .collect(Collectors.groupingBy(
                        Inventory::getDate,                  // group by date
                        Collectors.mapping(
                                Inventory::getPrice,        // extract price
                                Collectors.minBy(Comparator.naturalOrder()) // pick min
                        )
                ))
                .entrySet().stream()
                // Flatten Optional<BigDecimal> to BigDecimal
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().orElse(BigDecimal.ZERO)));

        // Prepare HotelMinPrice entities for bulk save
        List<HotelMinPrice> hotelPrices = new ArrayList<>();
        dailyMinPrices.forEach((date, price) -> {
            // Fetch existing record, or create new one if not found
            HotelMinPrice hotelPrice = hotelMinPriceRepository
                    .findByHotelAndDate(hotel, date)
                    .orElse(new HotelMinPrice(hotel, date));

            hotelPrice.setPrice(price); // update min price
            hotelPrices.add(hotelPrice);
        });

        // Save all min price records in batch
        hotelMinPriceRepository.saveAll(hotelPrices);
    }

    /**
     * Updates inventory prices using the dynamic pricing strategies.
     *
     * @param inventoryList List of inventory entries to update
     */
    private void updateInventoryPrices(List<Inventory> inventoryList) {
        inventoryList.forEach(inventory -> {
            // Calculate dynamic price using PricingService
            BigDecimal dynamicPrice = pricingService.calculateDynamicPricing(inventory);
            inventory.setPrice(dynamicPrice);
        });

        // Save all updated inventory records in bulk
        inventoryRepository.saveAll(inventoryList);
    }
}
