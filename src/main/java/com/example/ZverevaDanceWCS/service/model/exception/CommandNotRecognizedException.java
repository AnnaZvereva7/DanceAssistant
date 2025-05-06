package com.example.ZverevaDanceWCS.service.model.exception;

public class CommandNotRecognizedException extends RuntimeException{
    public CommandNotRecognizedException(String message) {
        super(message);
    }
}
