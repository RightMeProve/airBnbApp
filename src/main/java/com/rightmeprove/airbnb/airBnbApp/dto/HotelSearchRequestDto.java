package com.rightmeprove.airbnb.airBnbApp.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class HotelSearchRequestDto {
    private String city;
    // City where user wants to search hotels

    private LocalDate startDate;
    // Check-in date

    private LocalDate endDate;
    // Check-out date

    private Integer roomsCount;
    // Number of rooms required

    private Integer page = 0;
    // Pagination: page number (default = 0 → first page)

    private Integer size = 10;
    // Pagination: number of results per page (default = 10)
}
