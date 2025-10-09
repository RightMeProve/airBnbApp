package com.rightmeprove.airbnb.airBnbApp.exception;

/**
 * Custom exception thrown when a user tries to access a resource or action
 * they are not authorized to perform.
 * Extends RuntimeException → unchecked exception.
 * Typically mapped to HTTP 401 (Unauthorized) or 403 (Forbidden) in GlobalExceptionHandler.
 */
public class UnAuthorisedException extends RuntimeException {

    public UnAuthorisedException(String message){
        super(message); // Passes custom error message to the exception
    }
}
