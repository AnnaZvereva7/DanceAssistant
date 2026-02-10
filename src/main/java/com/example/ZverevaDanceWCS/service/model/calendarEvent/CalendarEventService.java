package com.example.ZverevaDanceWCS.service.model.calendarEvent;

import com.example.ZverevaDanceWCS.service.model.exception.UnavailableTimeExeption;
import com.example.ZverevaDanceWCS.service.model.freeSlots.FreeSlot;
import com.example.ZverevaDanceWCS.service.model.lessons.Lesson;
import com.example.ZverevaDanceWCS.service.model.lessons.LessonService;
import com.example.ZverevaDanceWCS.service.model.freeSlots.FreeSlotService;
import com.example.ZverevaDanceWCS.service.model.lessons.LessonStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CalendarEventService {
    final LessonService lessonService;
    final FreeSlotService slotService;

    public CalendarEventService(LessonService lessonService, FreeSlotService slotService) {
        this.lessonService = lessonService;
        this.slotService = slotService;
    }

    public void addFreeSlot(TimeRequest timeRequest, int trainerId) {
        slotService.addFreeTimeSlot(timeRequest.start, timeRequest.end, trainerId);
    }

    public List<CalendarEventDto> getTrainerCalendar(int trainerId) {
        return Stream
                .concat(getTrainerFreeSlots(trainerId).stream(),
                        getTrainerLessons(trainerId).stream())
                .collect(Collectors.toList());
    }

    public List<CalendarEventDto> getTrainerFreeSlots(int trainerId) {
        return slotService.findFreeSlotByTrainerId(trainerId)
                .stream()
                .map(CalendarEventDto::new)
                .toList();
    }

    public List<CalendarEventDto> getTrainerLessons(int trainerId) {
        return lessonService.findByStatusInAndTrainerId(List.of(LessonStatus.PLANNED, LessonStatus.PAID,
                LessonStatus.COMPLETED, LessonStatus.PENDING_STUDENT_CONFIRMATION, LessonStatus.PENDING_TRAINER_CONFIRMATION), trainerId)
                .stream()
                .map(CalendarEventDto::new)
                .toList();
    }

    public List<CalendarEventDto> getStudentLessons(int studentId) {
        return lessonService.findByStatusInAndStudentId(List.of(LessonStatus.PLANNED, LessonStatus.PAID,
                LessonStatus.COMPLETED, LessonStatus.PENDING_STUDENT_CONFIRMATION, LessonStatus.PENDING_TRAINER_CONFIRMATION), studentId)
                .stream()
                .map(CalendarEventDto::new)
                .toList();
    }

    public Long extractEventId(String eventId, String type) {
        String[] parts = eventId.split("-");
        if (parts.length == 2 && parts[0].equals(type)) {
            try {
                return Long.parseLong(parts[1]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid ID format: " + eventId);
            }
        } else {
            throw new IllegalArgumentException("Invalid ID format: " + eventId);
        }
    }

    public void deleteFreeSlot (String freeSlotID) {
        Long freeSlotId = extractEventId(freeSlotID, "slot");
        slotService.delete(freeSlotId);
    }

    public Lesson changeLessonTime (LocalDateTime newStart, LocalDateTime newEnd, Lesson oldLesson) {
        if (slotService.checkIfSlotFree(newStart, newEnd, oldLesson.getTrainer().getId())) {
            slotService.addFreeTimeSlot(oldLesson.getStartTime(), oldLesson.getEndTime(), oldLesson.getTrainer().getId());
            slotService.bookPartOfFreeSlot(newStart, newEnd, oldLesson.getTrainer().getId());
            oldLesson.setStartTime(newStart);
            oldLesson.setEndTime(newEnd);
            oldLesson.setDurationMin((int) Duration.between(oldLesson.getStartTime(), oldLesson.getEndTime()).toMinutes());
            return lessonService.updateLesson(oldLesson);
        } else {
            throw new UnavailableTimeExeption("This time in not free");
        }
    }
}
