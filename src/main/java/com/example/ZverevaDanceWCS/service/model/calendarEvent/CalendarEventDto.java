package com.example.ZverevaDanceWCS.service.model.calendarEvent;

import com.example.ZverevaDanceWCS.service.model.freeSlots.FreeSlot;
import com.example.ZverevaDanceWCS.service.model.lessons.Lesson;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import static java.lang.String.valueOf;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CalendarEventDto {
    private String id;
    private String title;
    private LocalDateTime start;
    private LocalDateTime end;
    private ExtendedProps extendedProps;

    public CalendarEventDto (FreeSlot slot) {
        this.id="slot-"+slot.getId().toString();
        this.title = "Free";
        this.start=slot.getStartTime();
        this.end=slot.getEndTime();
        this.extendedProps=new ExtendedProps(CalendarEventStatus.FREE);
    }

    public CalendarEventDto (Lesson lesson) {
        this.id="lesson-"+valueOf(lesson.getId());
        this.title = "Trainer: " + lesson.getTrainer().getName() + "\nStudent: " + lesson.getStudent().getName();
        this.start=lesson.getStartTime();
        this.end=lesson.getEndTime();
        switch (lesson.getStatus()) {
            case PLANNED -> this.extendedProps=new ExtendedProps(CalendarEventStatus.BOOKED);
            case PAID -> this.extendedProps=new ExtendedProps(CalendarEventStatus.BOOKED);
            case COMPLETED -> this.extendedProps=new ExtendedProps(CalendarEventStatus.BOOKED);
            case PENDING_STUDENT_CONFIRMATION -> this.extendedProps=new ExtendedProps(CalendarEventStatus.PENDING_STUDENT_CONFIRMATION);
            case PENDING_TRAINER_CONFIRMATION -> this.extendedProps=new ExtendedProps(CalendarEventStatus.PENDING_TRAINER_CONFIRMATION);
        }
        this.extendedProps.setTrainerId(lesson.getTrainer().getId());
    }

}
