package com.rightmeprove.airbnb.airBnbApp.exception;

public class UnAuthorisedException extends RuntimeException{
    public UnAuthorisedException(String message){
        super(message);
    }
}
