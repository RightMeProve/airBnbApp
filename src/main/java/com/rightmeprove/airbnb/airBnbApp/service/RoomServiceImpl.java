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

import static com.rightmeprove.airbnb.airBnbApp.util.AppUtils.getCurrentUser;

/**
 * Service implementation for managing hotel rooms.
 *
 * Responsibilities:
 * 1. Create, update, delete rooms.
 * 2. Fetch rooms for a given hotel.
 * 3. Ensure authorization checks (user must own the hotel).
 * 4. Interact with InventoryService to initialize/delete inventory for rooms.
 */
@Service
@RequiredArgsConstructor // generates constructor for all final dependencies (DI)
@Slf4j
public class RoomServiceImpl implements RoomService {

    // Repository to manage Room entities
    private final RoomRepository roomRepository;

    // Repository to manage Hotel entities
    private final HotelRepository hotelRepository;

    // ModelMapper for mapping between entity <-> DTO
    private final ModelMapper modelMapper;

    // Inventory service to initialize or delete inventory for rooms
    private final InventoryService inventoryService;

    /**
     * Creates a new room under a specific hotel.
     *
     * @param hotelId ID of the hotel
     * @param roomDto DTO containing room details
     * @return Created RoomDto
     */
    @Override
    @Transactional
    public RoomDto createNewRoom(Long hotelId, RoomDto roomDto) {
        log.info("Creating a new room in hotel with ID: {}", hotelId);

        // Fetch hotel and validate existence
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: " + hotelId));

        // Check ownership: only the hotel owner can create rooms
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner())) {
            throw new UnAuthorisedException("This user does not own this hotel with ID: " + hotelId);
        }

        // Map DTO → entity and set hotel relationship
        Room room = modelMapper.map(roomDto, Room.class);
        room.setHotel(hotel);

        // Persist room
        room = roomRepository.save(room);

        // If hotel is active, initialize inventory for the new room for 1 year
        if (hotel.getActive()) {
            inventoryService.initializeRoomForAYear(room);
        }

        // Map back to DTO for response
        return modelMapper.map(room, RoomDto.class);
    }

    /**
     * Fetches all rooms for a given hotel.
     *
     * @param hotelId ID of the hotel
     * @return List of RoomDto
     */
    @Override
    public List<RoomDto> getAllRoomsInHotel(Long hotelId) {
        log.info("Getting all the rooms of hotel with ID: {}", hotelId);

        // Validate hotel existence
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: " + hotelId));

        // Check ownership
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner())) {
            throw new UnAuthorisedException("This user does not own this hotel with ID: " + hotelId);
        }

        // Map each Room entity → RoomDto
        return hotel.getRooms()
                .stream()
                .map(room -> modelMapper.map(room, RoomDto.class))
                .collect(Collectors.toList());
    }

    /**
     * Fetch a single room by its ID.
     *
     * @param roomId ID of the room
     * @return RoomDto
     */
    @Override
    public RoomDto getRoomById(Long roomId) {
        log.info("Getting the room with ID: {}", roomId);

        // Fetch room or throw exception
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + roomId));

        return modelMapper.map(room, RoomDto.class);
    }

    /**
     * Deletes a room and its inventory.
     *
     * @param roomId ID of the room to delete
     */
    @Override
    @Transactional
    public void deleteRoomById(Long roomId) {
        log.info("Deleting the room with ID: {}", roomId);

        // Fetch room and validate
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + roomId));

        // Check ownership
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(room.getHotel().getOwner())) {
            throw new UnAuthorisedException("This user does not own this room with ID: " + roomId);
        }

        // Delete inventories first to avoid orphaned inventory records
        inventoryService.deleteAllInventories(room);

        // Delete the room
        roomRepository.deleteById(roomId);
    }

    /**
     * Updates an existing room.
     *
     * @param hotelId ID of the hotel
     * @param roomId  ID of the room
     * @param roomDto DTO with updated room info
     * @return Updated RoomDto
     */
    @Override
    @Transactional
    public RoomDto updateRoomById(Long hotelId, Long roomId, RoomDto roomDto) {
        log.info("Updating the room with ID: {}", roomId);

        // Validate hotel existence and ownership
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: " + hotelId));
        User user = getCurrentUser();
        if(!user.equals(hotel.getOwner())) {
            throw new UnAuthorisedException("This user does not own this hotel with ID: " + hotelId);
        }

        // Validate room existence
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + roomId));

        // Map updated DTO → entity
        modelMapper.map(roomDto, room);
        room.setId(roomId);

        // Save changes
        room = roomRepository.save(room);

        return modelMapper.map(room, RoomDto.class);
    }
}
