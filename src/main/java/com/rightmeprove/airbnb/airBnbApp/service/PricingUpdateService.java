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
 * Service that periodically updates:
 * 1. Inventory prices (using dynamic pricing strategies).
 * 2. Minimum daily hotel price (stored in HotelMinPrice table for fast lookups).
 *
 * Runs automatically every hour using @Scheduled.
 */
@Service
@RequiredArgsConstructor // generates constructor for all final fields
@Slf4j
@Transactional
public class PricingUpdateService {

    // Dependencies
    private final HotelRepository hotelRepository;
    private final InventoryRepository inventoryRepository;
    private final HotelMinPriceRepository hotelMinPriceRepository;
    private final PricingService pricingService;

    /**
     * Scheduled job that runs every hour (cron = top of every hour).
     * Updates all hotels’ inventory and min price data in batches.
     */
    @Scheduled(cron = "0 0 * * * *")
    public void updatePrices() {
        int page = 0;
        int batchSize = 100; // process hotels in chunks to avoid memory overload

        while (true) {
            Page<Hotel> hotelPage = hotelRepository.findAll(PageRequest.of(page, batchSize));

            if (hotelPage.isEmpty()) {
                break; // stop if no more hotels
            }

            // Update prices for each hotel in this batch
            hotelPage.getContent().forEach(this::updateHotelPrices);
            page++;
        }
    }

    /**
     * Updates inventory prices + hotel minimum daily prices for a given hotel.
     */
    private void updateHotelPrices(Hotel hotel) {
        log.info("Updating hotel prices for hotel ID: {}", hotel.getId());

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusYears(1);

        // Fetch all inventory entries for this hotel for the next year
        List<Inventory> inventoryList =
                inventoryRepository.findByHotelAndDateBetween(hotel, startDate, endDate);

        // Apply dynamic pricing strategies
        updateInventoryPrices(inventoryList);

        // Compute and persist minimum daily price for the hotel
        updateHotelMinPrice(hotel, inventoryList, startDate, endDate);
    }

    /**
     * Updates the HotelMinPrice table with the lowest price per day
     * across all rooms in the hotel.
     */
    private void updateHotelMinPrice(Hotel hotel, List<Inventory> inventoryList,
                                     LocalDate startDate, LocalDate endDate) {

        // Compute minimum price per day for the hotel
        Map<LocalDate, BigDecimal> dailyMinPrices = inventoryList.stream()
                .collect(Collectors.groupingBy(
                        Inventory::getDate, // group by date
                        Collectors.mapping(Inventory::getPrice,
                                Collectors.minBy(Comparator.naturalOrder())) // pick min price
                ))
                .entrySet().stream()
                // flatten Optional<BigDecimal> → BigDecimal
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().orElse(BigDecimal.ZERO)));

        // Prepare HotelMinPrice entities (bulk update)
        List<HotelMinPrice> hotelPrices = new ArrayList<>();
        dailyMinPrices.forEach((date, price) -> {
            // Find existing record, or create new one if missing
            HotelMinPrice hotelPrice = hotelMinPriceRepository
                    .findByHotelAndDate(hotel, date)
                    .orElse(new HotelMinPrice(hotel, date));

            hotelPrice.setPrice(price);
            hotelPrices.add(hotelPrice);
        });

        // Save all records in batch
        hotelMinPriceRepository.saveAll(hotelPrices);
    }

    /**
     * Applies dynamic pricing to each inventory record and updates DB.
     */
    private void updateInventoryPrices(List<Inventory> inventoryList) {
        inventoryList.forEach(inventory -> {
            // Calculate price using PricingService (chain of strategies)
            BigDecimal dynamicPrice = pricingService.calculateDynamicPricing(inventory);
            inventory.setPrice(dynamicPrice);
        });

        // Bulk save updated inventories
        inventoryRepository.saveAll(inventoryList);
    }
}
