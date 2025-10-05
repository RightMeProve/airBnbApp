package com.rightmeprove.airbnb.airBnbApp.service;

import com.rightmeprove.airbnb.airBnbApp.dto.HotelDto;
import com.rightmeprove.airbnb.airBnbApp.dto.HotelPriceDto;
import com.rightmeprove.airbnb.airBnbApp.dto.HotelSearchRequestDto;
import com.rightmeprove.airbnb.airBnbApp.dto.InventoryDto;
import com.rightmeprove.airbnb.airBnbApp.entity.Inventory;
import com.rightmeprove.airbnb.airBnbApp.entity.Room;
import com.rightmeprove.airbnb.airBnbApp.entity.User;
import com.rightmeprove.airbnb.airBnbApp.exception.ResourceNotFoundException;
import com.rightmeprove.airbnb.airBnbApp.exception.UnAuthorisedException;
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
/*
 * Why @RequiredArgsConstructor?
 * - Generates a constructor with all final fields (inventoryRepository, modelMapper).
 * - Spring uses this constructor for dependency injection.
 *
 * Why not @NoArgsConstructor?
 * - That would create an empty constructor with no params.
 * - Spring cannot inject final fields with it → dependencies would remain null.
 *
 * Why not @Data?
 * - @Data = getters, setters, toString, equals, hashCode, and RequiredArgsConstructor.
 * - Services don’t need setters or equals/hashCode (unlike entities/DTOs).
 * - Cleaner to only use @RequiredArgsConstructor here.
 */
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ModelMapper modelMapper;
    private final HotelMinPriceRepository hotelMinPriceRepository;
    private final RoomRepository roomRepository;

    @Override
    @Transactional
    public void initializeRoomForAYear(Room room) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusYears(1);

        // Loop through 365+ days and create inventory records for each day
        for (; !today.isAfter(endDate); today = today.plusDays(1)) {
            Inventory inventory = Inventory.builder()
                    .hotel(room.getHotel())               // link to hotel
                    .room(room)                           // link to room
                    .city(room.getHotel().getCity())      // duplicate city for faster search
                    .date(today)                          // inventory date
                    .reservedCount(0)                     // default: no rooms reserved
                    .price(room.getBasePrice())           // base price for the room
                    .surgeFactor(BigDecimal.ONE)          // default = no surge
                    .totalCount(room.getTotalCount())     // number of rooms available
                    .closed(false)                        // default = open
                    .build();
            inventoryRepository.save(inventory);
        }
    }

    @Override
    @Transactional
    public void deleteAllInventories(Room room) {
        log.info("Deleting the inventories of room with ID: {}", room.getId());
        inventoryRepository.deleteByRoom(room); // bulk delete
    }

    @Override
    public Page<HotelPriceDto> searchHotels(HotelSearchRequestDto hotelSearchRequestDto) {
        log.info("Searching hotels for {} city, from {} to {} ",
                hotelSearchRequestDto.getCity(),
                hotelSearchRequestDto.getStartDate(),
                hotelSearchRequestDto.getEndDate());

        // Pagination (page number + size from request)
        Pageable pageable = PageRequest.of(
                hotelSearchRequestDto.getPage(),
                hotelSearchRequestDto.getSize()
        );

        // Count how many days user wants to stay (inclusive)
        long dateCount = ChronoUnit.DAYS.between(
                hotelSearchRequestDto.getStartDate(),
                hotelSearchRequestDto.getEndDate()
        ) + 1;

        // Query DB for hotels with inventory available for all requested days
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

    @Override
    public List<InventoryDto> getAllInventoryByRoom(Long roomId) {
        log.info("Getting all inventory by room for room with id: {}",roomId);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(()->new ResourceNotFoundException("Room not found with id: "+roomId));

        User user = getCurrentUser();
        if(!user.equals(room.getHotel().getOwner()))
        {
            throw new AccessDeniedException("You are not the owner of the room with Id: "+roomId);
        }

        return inventoryRepository.findByRoomOrderByDate(room).stream()
                .map((element)->modelMapper.map(element,
                        InventoryDto.class))
                .collect(Collectors.toList());
    }
}
