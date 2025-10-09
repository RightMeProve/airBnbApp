package com.rightmeprove.airbnb.airBnbApp.controller;

import com.rightmeprove.airbnb.airBnbApp.dto.HotelDto;
import com.rightmeprove.airbnb.airBnbApp.dto.HotelInfoDto;
import com.rightmeprove.airbnb.airBnbApp.dto.HotelPriceDto;
import com.rightmeprove.airbnb.airBnbApp.dto.HotelSearchRequestDto;
import com.rightmeprove.airbnb.airBnbApp.service.HotelService;
import com.rightmeprove.airbnb.airBnbApp.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hotels") // Base route for all hotel-related endpoints
@RequiredArgsConstructor // Auto-injects required dependencies via constructor
public class HotelBrowserController {

    private final InventoryService inventoryService;
    private final HotelService hotelService;

    /**
     * Searches available hotels based on filters like city, date range, guests, etc.
     * @param hotelSearchRequestDto search criteria (location, check-in/out, guests)
     * @return paginated list of matching hotels with pricing info
     */
    @GetMapping("/search")
    public ResponseEntity<Page<HotelPriceDto>> searchHotels(@RequestBody HotelSearchRequestDto hotelSearchRequestDto) {
        Page<HotelPriceDto> page = inventoryService.searchHotels(hotelSearchRequestDto);
        return ResponseEntity.ok(page);
    }

    /**
     * Fetches detailed information about a specific hotel.
     * @param hotelId unique ID of the hotel
     * @return full hotel details (description, rooms, amenities, etc.)
     */
    @GetMapping("/{hotelId}/info")
    public ResponseEntity<HotelInfoDto> getHotelInfo(@PathVariable Long hotelId) {
        return ResponseEntity.ok(hotelService.getHotelInfoById(hotelId));
    }
}
