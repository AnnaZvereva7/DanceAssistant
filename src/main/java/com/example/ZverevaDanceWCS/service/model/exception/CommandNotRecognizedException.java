package com.example.ZverevaDanceWCS.service.model.exception;

import com.example.ZverevaDanceWCS.service.Constant;

public class CommandNotRecognizedException extends RuntimeException{
    public CommandNotRecognizedException() {
        super(Constant.CNR);
    }
}
//todo add exception handler class and add notification in telegram, but only for command from telegram