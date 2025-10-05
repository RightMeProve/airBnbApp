package com.rightmeprove.airbnb.airBnbApp.service;

import com.rightmeprove.airbnb.airBnbApp.dto.HotelPriceDto;
import com.rightmeprove.airbnb.airBnbApp.dto.HotelSearchRequestDto;
import com.rightmeprove.airbnb.airBnbApp.dto.InventoryDto;
import com.rightmeprove.airbnb.airBnbApp.dto.UpdateInventoryRequestDto;
import com.rightmeprove.airbnb.airBnbApp.entity.Room;
import org.springframework.data.domain.Page;

import java.util.List;

public interface InventoryService {

    void initializeRoomForAYear(Room room);

    void deleteAllInventories(Room room);

    Page<HotelPriceDto> searchHotels(HotelSearchRequestDto hotelSearchRequest);

    List<InventoryDto> getAllInventoryByRoom(Long roomId);

    void updateInventory(Long roomId, UpdateInventoryRequestDto updateInventoryRequestDto);
}
