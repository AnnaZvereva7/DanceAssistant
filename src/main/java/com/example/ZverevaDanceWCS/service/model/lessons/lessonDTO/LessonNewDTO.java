package com.example.ZverevaDanceWCS.service.model.lessons.lessonDTO;

import com.example.ZverevaDanceWCS.service.model.lessons.Lesson;
import com.example.ZverevaDanceWCS.service.model.lessons.LessonStatus;
import com.example.ZverevaDanceWCS.service.model.user.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class LessonNewDTO {
    @NotNull
    @Positive
    int studentId;

    @JsonFormat(pattern = "dd.MM.yyyy HH:mm")
    @Future
    LocalDateTime startTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    LessonStatus status;

    int durationMin;
    int cost;

    public Lesson newLessonFromJson(User student, User trainer) {
        Lesson newLesson = new Lesson();
        newLesson.setStudent(student);
        newLesson.setStartTime(this.startTime);
        newLesson.setStatus(this.status);
        newLesson.setTrainer(trainer);
        if (this.getCost() > 0) {
            newLesson.setCost(this.cost);
        } else {
            newLesson.setCost(0);
        }
        log.info("duration ="+this.durationMin);
        if(this.durationMin ==0){
            newLesson.setDurationMin(60);
        } else {
            newLesson.setDurationMin(this.durationMin);
        }
        newLesson.setEndTime(this.getStartTime().plusMinutes(newLesson.getDurationMin()));
        newLesson.setTitle(student.getName()+" WCS Lesson");
        return newLesson;
    }
}
