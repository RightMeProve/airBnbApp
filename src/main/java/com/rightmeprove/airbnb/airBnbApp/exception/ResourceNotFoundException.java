package com.rightmeprove.airbnb.airBnbApp.exception;

/**
 * Custom exception thrown when a requested resource (e.g., Hotel, Booking, User) is not found.
 * Extends RuntimeException → unchecked exception.
 * Will be handled globally by GlobalExceptionHandler to return 404 responses.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message){
        super(message); // Passes custom error message to the exception
    }

}
