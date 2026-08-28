package com.example.ZverevaDanceWCS.service.model.appCalendarEvent;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ExtendedProps {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    CalendarEventStatus status;
    Long trainerId;

    public ExtendedProps (CalendarEventStatus status) {
        this.status=status;
        this.trainerId=null;
    }
}
