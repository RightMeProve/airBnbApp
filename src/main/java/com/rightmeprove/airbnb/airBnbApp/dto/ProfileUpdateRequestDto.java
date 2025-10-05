package com.rightmeprove.airbnb.airBnbApp.dto;

import com.rightmeprove.airbnb.airBnbApp.entity.enums.Gender;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProfileUpdateRequestDto {
    private String name;
    private LocalDate dateOfBirth;
    private Gender gender;
}
