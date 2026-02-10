package com.example.ZverevaDanceWCS.service.model.user;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum UserSiteStatus {
    ADMIN,
    TRAINER,
    ACTIVE,
    UNAUTHORIZED,
    BLOCKED;
}
