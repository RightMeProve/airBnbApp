package com.rightmeprove.airbnb.airBnbApp.service;

import com.rightmeprove.airbnb.airBnbApp.dto.RoomDto;
import com.rightmeprove.airbnb.airBnbApp.entity.Hotel;
import com.rightmeprove.airbnb.airBnbApp.entity.Room;
import com.rightmeprove.airbnb.airBnbApp.exception.ResourceNotFoundException;
import com.rightmeprove.airbnb.airBnbApp.repository.HotelRepository;
import com.rightmeprove.airbnb.airBnbApp.repository.RoomRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Data
@Slf4j
public class RoomServiceImpl implements RoomService{

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;
    private final InventoryService inventoryService;

    @Override
    public RoomDto createNewRoom(Long hotelId,RoomDto roomDto) {
        log.info("Creating a new room in hotel with ID: {}",hotelId);
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with ID: {}"+ hotelId));
        Room room = modelMapper.map(roomDto,Room.class);
        room.setHotel(hotel);
        room = roomRepository.save(room);

        if(hotel.getActive())
        {
            inventoryService.initializeRoomForAYear(room);
        }

        return modelMapper.map(room,RoomDto.class);
    }

    @Override
    public List<RoomDto> getAllRoomsInHotel(Long hotelId) {
        log.info("Getting all the room of hotel with ID: {}",hotelId);
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with ID: "+hotelId));
        return hotel.getRooms()
                .stream()
                .map((element) -> modelMapper.map(element,RoomDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public RoomDto getRoomById(Long roomId) {
        log.info("Getting the room with ID: {}",roomId);
        Room room = roomRepository
                .findById(roomId)
                .orElseThrow(()->new ResourceNotFoundException("Room not found with ID: " +roomId));
        return modelMapper.map(room,RoomDto.class);
    }

    @Override
    public void deleteRoomById(Long roomId) {
        log.info("Deleting the room with ID: "+roomId);
        Room room = roomRepository
                .findById(roomId)
                .orElseThrow(()->(new ResourceNotFoundException("Room not found with roomID: "+roomId)));
        roomRepository.deleteById(roomId);
        inventoryService.deleteFutureInventories(room);

    }
}
