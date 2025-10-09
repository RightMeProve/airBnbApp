package com.rightmeprove.airbnb.airBnbApp.advice;

import lombok.Data;
import java.time.LocalDateTime;

@Data // Lombok annotation — generates getters, setters, toString, equals, and hashCode
public class ApiResponse<T> {

    // Timestamp for when the response was created (helps in debugging/logging)
    private LocalDateTime timeStamp;

    // Generic data field — holds the actual success response (e.g., user, booking info, etc.)
    private T data;

    // Holds error details if the response represents a failure
    private ApiError error;

    // Default constructor — sets current timestamp automatically
    public ApiResponse() {
        this.timeStamp = LocalDateTime.now();
    }

    // Constructor for successful responses
    public ApiResponse(T data) {
        this(); // calls default constructor to set timestamp
        this.data = data;
    }

    // Constructor for error responses
    public ApiResponse(ApiError error) {
        this(); // ensures timestamp is always set
        this.error = error;
    }
}
