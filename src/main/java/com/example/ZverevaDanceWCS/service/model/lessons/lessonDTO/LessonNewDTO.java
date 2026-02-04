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

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LessonNewDTO {
    @NotNull
    @Positive
    int studentId;

    @JsonFormat(pattern = "dd.MM.yyyy HH:mm")
    @Future
    LocalDateTime startTime;

    int durationMinutes;
    int cost;

    public Lesson newLessonFromJson(User student, User trainer) {
        Lesson newLesson = new Lesson();
        newLesson.setStudent(student);
        newLesson.setStartTime(this.startTime);
        newLesson.setStatus(LessonStatus.PLANNED);
        newLesson.setTrainer(trainer);
        if (this.getCost() > 0) {
            newLesson.setCost(this.cost);
        } else {
            newLesson.setCost(0);
        }
        if(this.durationMinutes==0){
            newLesson.setDurationMin(60);
        } else {
            newLesson.setDurationMin(this.durationMinutes);
        }
        newLesson.setEndTime(this.getStartTime().plusMinutes(newLesson.getDurationMin()));
        newLesson.setTitle(student.getName()+" WCS Lesson");
        return newLesson;
    }
}
