package com.rightmeprove.airbnb.airBnbApp.service;

import com.rightmeprove.airbnb.airBnbApp.dto.RoomDto;
import com.rightmeprove.airbnb.airBnbApp.entity.Hotel;
import com.rightmeprove.airbnb.airBnbApp.entity.Room;
import com.rightmeprove.airbnb.airBnbApp.entity.User;
import com.rightmeprove.airbnb.airBnbApp.exception.ResourceNotFoundException;
import com.rightmeprove.airbnb.airBnbApp.exception.UnAuthorisedException;
import com.rightmeprove.airbnb.airBnbApp.repository.HotelRepository;
import com.rightmeprove.airbnb.airBnbApp.repository.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
/*
 * ⚠️ @Data is removed here
 * - @Data generates setters, equals, hashCode, toString → not needed in service classes.
 * - Best practice: use @RequiredArgsConstructor + @Slf4j for clean, immutable service beans.
 */
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;
    private final InventoryService inventoryService;

    @Override
    @Transactional
    public RoomDto createNewRoom(Long hotelId, RoomDto roomDto) {
        log.info("Creating a new room in hotel with ID: {}", hotelId);

        // Find hotel or throw exception if not found
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: " + hotelId));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner()))
        {
            throw new UnAuthorisedException("This user does not own this hotel with id: "+hotelId);
        }

        // Map DTO → entity
        Room room = modelMapper.map(roomDto, Room.class);
        room.setHotel(hotel);

        // Save new room
        room = roomRepository.save(room);

        // If hotel is active, initialize inventory for 1 year
        if (hotel.getActive()) {
            inventoryService.initializeRoomForAYear(room);
        }

        // Map back to DTO for response
        return modelMapper.map(room, RoomDto.class);
    }

    @Override
    public List<RoomDto> getAllRoomsInHotel(Long hotelId) {
        log.info("Getting all the rooms of hotel with ID: {}", hotelId);

        // Ensure hotel exists
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: " + hotelId));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner()))
        {
            throw new UnAuthorisedException("This user does not own this hotel with id: "+hotelId);
        }

        // Map each Room → RoomDto
        return hotel.getRooms()
                .stream()
                .map(room -> modelMapper.map(room, RoomDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public RoomDto getRoomById(Long roomId) {
        log.info("Getting the room with ID: {}", roomId);

        // Fetch room or fail
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + roomId));

        return modelMapper.map(room, RoomDto.class);
    }

    @Override
    @Transactional
    public void deleteRoomById(Long roomId) {
        log.info("Deleting the room with ID: {}", roomId);

        // Ensure room exists
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with roomID: " + roomId));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(room.getHotel().getOwner()))
        {
            throw new UnAuthorisedException("This user does not own this room with id: "+roomId);
        }

        // Delete inventories first (avoid orphaned records)
        inventoryService.deleteAllInventories(room);

        // Delete the room
        roomRepository.deleteById(roomId);
    }
}
