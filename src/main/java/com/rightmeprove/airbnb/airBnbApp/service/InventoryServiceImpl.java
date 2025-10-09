package com.rightmeprove.airbnb.airBnbApp.service;

import com.rightmeprove.airbnb.airBnbApp.dto.*;
import com.rightmeprove.airbnb.airBnbApp.entity.Inventory;
import com.rightmeprove.airbnb.airBnbApp.entity.Room;
import com.rightmeprove.airbnb.airBnbApp.entity.User;
import com.rightmeprove.airbnb.airBnbApp.exception.ResourceNotFoundException;
import com.rightmeprove.airbnb.airBnbApp.repository.HotelMinPriceRepository;
import com.rightmeprove.airbnb.airBnbApp.repository.InventoryRepository;
import com.rightmeprove.airbnb.airBnbApp.repository.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static com.rightmeprove.airbnb.airBnbApp.util.AppUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;     // CRUD & custom inventory queries
    private final ModelMapper modelMapper;                     // Entity ↔ DTO conversion
    private final HotelMinPriceRepository hotelMinPriceRepository; // Custom query for searching hotels
    private final RoomRepository roomRepository;               // For verifying room existence

    /**
     * Initialize inventory for a room for 1 year.
     * - Creates daily Inventory rows with default values.
     * - Links each inventory to the hotel, room, and city.
     */
    @Override
    @Transactional
    public void initializeRoomForAYear(Room room) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusYears(1);

        // Loop through all days from today to 1 year ahead
        for (; !today.isAfter(endDate); today = today.plusDays(1)) {
            Inventory inventory = Inventory.builder()
                    .hotel(room.getHotel())               // link inventory to hotel
                    .room(room)                           // link to room
                    .city(room.getHotel().getCity())      // store city for faster queries
                    .date(today)                          // inventory date
                    .reservedCount(0)                     // no rooms reserved initially
                    .price(room.getBasePrice())           // base price for the room
                    .surgeFactor(BigDecimal.ONE)          // no surge initially
                    .totalCount(room.getTotalCount())     // total available rooms
                    .closed(false)                        // room is open for booking
                    .build();
            inventoryRepository.save(inventory);
        }
    }

    /**
     * Delete all inventory rows for a room.
     * - Used when removing a room or hotel.
     */
    @Override
    @Transactional
    public void deleteAllInventories(Room room) {
        log.info("Deleting the inventories of room with ID: {}", room.getId());
        inventoryRepository.deleteByRoom(room); // bulk delete for efficiency
    }

    /**
     * Search hotels based on availability and requested dates.
     * - Uses custom repository query to return hotels with sufficient inventory.
     * - Returns a paginated result of HotelPriceDto.
     */
    @Override
    public Page<HotelPriceDto> searchHotels(HotelSearchRequestDto hotelSearchRequestDto) {
        log.info("Searching hotels for {} city, from {} to {} ",
                hotelSearchRequestDto.getCity(),
                hotelSearchRequestDto.getStartDate(),
                hotelSearchRequestDto.getEndDate());

        // Setup pagination
        Pageable pageable = PageRequest.of(
                hotelSearchRequestDto.getPage(),
                hotelSearchRequestDto.getSize()
        );

        // Number of days requested (inclusive)
        long dateCount = ChronoUnit.DAYS.between(
                hotelSearchRequestDto.getStartDate(),
                hotelSearchRequestDto.getEndDate()
        ) + 1;

        // Query DB for hotels with enough inventory for all requested days
        Page<HotelPriceDto> hotelPage = hotelMinPriceRepository.findHotelWithAvailableInventory(
                hotelSearchRequestDto.getCity(),
                hotelSearchRequestDto.getStartDate(),
                hotelSearchRequestDto.getEndDate(),
                hotelSearchRequestDto.getRoomsCount(),
                dateCount,
                pageable
        );

        return hotelPage;
    }

    /**
     * Get all inventory for a given room.
     * - Checks that the current user owns the hotel.
     * - Returns inventory mapped as InventoryDto.
     */
    @Override
    public List<InventoryDto> getAllInventoryByRoom(Long roomId) {
        log.info("Getting all inventory by room for room with id: {}", roomId);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));

        // Ensure user owns the hotel
        User user = getCurrentUser();
        if (!user.equals(room.getHotel().getOwner())) {
            throw new AccessDeniedException("You are not the owner of the room with Id: " + roomId);
        }

        return inventoryRepository.findByRoomOrderByDate(room).stream()
                .map(inventory -> modelMapper.map(inventory, InventoryDto.class))
                .collect(Collectors.toList());
    }

    /**
     * Update inventory for a room between two dates.
     * - Locks inventory rows for consistency.
     * - Updates closed status and surge factor.
     */
    @Override
    @Transactional
    public void updateInventory(Long roomId, UpdateInventoryRequestDto updateInventoryRequestDto) {
        log.info("Updating all inventory by room for room with id: {} between date range: {} - {}",
                roomId, updateInventoryRequestDto.getStartDate(), updateInventoryRequestDto.getEndDate());

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));

        // Check ownership
        User user = getCurrentUser();
        if (!user.equals(room.getHotel().getOwner())) {
            throw new AccessDeniedException("You are not the owner of the room with Id: " + roomId);
        }

        // Lock inventory rows before updating to prevent race conditions
        inventoryRepository.getInventoryAndLockBeforeUpdate(
                roomId,
                updateInventoryRequestDto.getStartDate(),
                updateInventoryRequestDto.getEndDate()
        );

        // Update inventory fields
        inventoryRepository.updateInventory(
                roomId,
                updateInventoryRequestDto.getStartDate(),
                updateInventoryRequestDto.getEndDate(),
                updateInventoryRequestDto.getClosed(),
                updateInventoryRequestDto.getSurgeFactor()
        );
    }
}
