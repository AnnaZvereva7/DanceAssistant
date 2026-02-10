package com.example.ZverevaDanceWCS.service.model.calendarEvent;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TimeRequest {
    LocalDateTime start;
    LocalDateTime end;
}
