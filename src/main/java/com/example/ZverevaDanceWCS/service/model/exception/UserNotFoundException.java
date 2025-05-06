package com.example.ZverevaDanceWCS.service.model.exception;


public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException (String message) {
        super(message);
    }
}
