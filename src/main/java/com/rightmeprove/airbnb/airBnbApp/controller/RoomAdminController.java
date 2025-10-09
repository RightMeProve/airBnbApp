package com.rightmeprove.airbnb.airBnbApp.controller;

import com.rightmeprove.airbnb.airBnbApp.dto.RoomDto;
import com.rightmeprove.airbnb.airBnbApp.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/hotels/{hotelId}/rooms") // Admin endpoints for managing rooms within a hotel
@RequiredArgsConstructor // Injects RoomService automatically
public class RoomAdminController {

    private final RoomService roomService;

    /**
     * Creates a new room for a specific hotel.
     * @param hotelId ID of the hotel
     * @param roomDto details of the new room
     * @return created RoomDto with 201 CREATED
     */
    @PostMapping
    public ResponseEntity<RoomDto> createNewRoom(@PathVariable Long hotelId,
                                                 @RequestBody RoomDto roomDto) {
        RoomDto room = roomService.createNewRoom(hotelId, roomDto);
        return new ResponseEntity<>(room, HttpStatus.CREATED);
    }

    /**
     * Retrieves all rooms for a given hotel.
     * @param hotelId ID of the hotel
     * @return list of RoomDto
     */
    @GetMapping
    public ResponseEntity<List<RoomDto>> getAllRoomsInHotel(@PathVariable Long hotelId) {
        return ResponseEntity.ok(roomService.getAllRoomsInHotel(hotelId));
    }

    /**
     * Retrieves a specific room by ID.
     * @param roomId ID of the room
     * @return RoomDto
     */
    @GetMapping("/{roomId}")
    public ResponseEntity<RoomDto> getRoomById(@PathVariable Long hotelId, @PathVariable Long roomId) {
        return ResponseEntity.ok(roomService.getRoomById(roomId));
    }

    /**
     * Deletes a specific room.
     * @param roomId ID of the room
     * @return 204 No Content
     */
    @DeleteMapping("/{roomId}")
    public ResponseEntity<RoomDto> deleteRoomById(@PathVariable Long hotelId, @PathVariable Long roomId) {
        roomService.deleteRoomById(roomId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Updates details of a specific room.
     * @param roomId ID of the room
     * @param roomDto updated room information
     * @return updated RoomDto
     */
    @PutMapping("/{roomId}")
    public ResponseEntity<RoomDto> updateRoomById(@PathVariable Long hotelId, @PathVariable Long roomId,
                                                  @RequestBody RoomDto roomDto) {
        return ResponseEntity.ok(roomService.updateRoomById(hotelId, roomId, roomDto));
    }
}
