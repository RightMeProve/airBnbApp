package com.rightmeprove.airbnb.airBnbApp.service;

import com.rightmeprove.airbnb.airBnbApp.dto.HotelDto;
import com.rightmeprove.airbnb.airBnbApp.dto.RoomDto;
import com.rightmeprove.airbnb.airBnbApp.entity.Hotel;
import com.rightmeprove.airbnb.airBnbApp.entity.Room;
import com.rightmeprove.airbnb.airBnbApp.exception.ResourceNotFoundException;
import com.rightmeprove.airbnb.airBnbApp.repository.HotelRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;
    private final InventoryService inventoryService;
    @Override
    public HotelDto createNewHotel(HotelDto hotelDto) {
        log.info("Creating new hotel info with name: {}",hotelDto.getName());
        Hotel hotel = modelMapper.map(hotelDto,Hotel.class);
        hotel.setActive(false);
        hotel = hotelRepository.save(hotel);
        log.info("Created a new hotel with ID: {}",hotel.getId());
        return modelMapper.map(hotel,HotelDto.class);
    }

    @Override
    public HotelDto getHotelById(Long id) {
        log.info("Getting the hotel with ID: {}",id);
        Hotel hotel = hotelRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel was not found with ID:" + id));
        return modelMapper.map(hotel,HotelDto.class);
    }

    @Override
    public HotelDto updateHotelById(Long id,HotelDto hotelDto){
        log.info("Updating the hotel with ID: {}",id);
        Hotel hotel = hotelRepository
                .findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Hotel was not found with ID: " + id));
        modelMapper.map(hotelDto,hotel);
        hotel.setId(id);
        hotel = hotelRepository.save(hotel);
        return modelMapper.map(hotel,HotelDto.class);
    }

    @Override
    @Transactional
    public void deleteHotelById(Long id){
        Hotel hotel = hotelRepository
                .findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Hotel was not found with ID: "+id));
        hotelRepository.deleteById(id);
        for(Room room : hotel.getRooms())
        {
            inventoryService.deleteFutureInventories(room);
        }

    }

    @Override
    @Transactional
    public void activateHotel(Long hotelId) {
        log.info("Activating the hotel with ID: {}",hotelId);
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(()-> new ResourceNotFoundException("Hotel not found with ID:" + hotelId));

        hotel.setActive(true);
        // assuming only do it once
        for(Room room:hotel.getRooms()){
            inventoryService.initializeRoomForAYear(room);
        }
    }

    @Override
    public List<HotelDto> getAllHotels() {
        log.info("Getting all hotels");
        return hotelRepository.findAll()
                .stream()
                .map(hotel -> modelMapper.map(hotel, HotelDto.class))
                .collect(Collectors.toList());
    }



}
