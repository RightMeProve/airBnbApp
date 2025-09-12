package com.rightmeprove.airbnb.airBnbApp.entity;

import jakarta.persistence.Embeddable;  // Marks class as embeddable in another entity
import lombok.Getter;  // Lombok auto-generates getter methods
import lombok.Setter;  // Lombok auto-generates setter methods

@Getter
@Setter
@Embeddable  // This class is not a separate table, but embedded inside another entity
public class HotelContactInfo {

    private String address;       // Street address of the hotel
    private String phoneNumber;   // Contact phone number
    private String email;         // Contact email
    private String location;      // Could be city/state/coordinates, depending on use
}
