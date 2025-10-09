package com.rightmeprove.airbnb.airBnbApp.advice;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.List;

@Data // Lombok annotation that auto-generates getters, setters, toString, equals, and hashCode methods
@Builder // Enables the Builder pattern for flexible object creation (ApiError.builder()...)
public class ApiError {

    // Represents the HTTP status of the error (e.g., 404 NOT_FOUND, 400 BAD_REQUEST)
    private HttpStatus status;

    // Main error message (e.g., "User not found" or "Invalid credentials")
    private String message;

    // Optional list of detailed sub-errors, such as validation errors for multiple fields
    private List<String> subErrors;
}
