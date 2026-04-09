package com.example.ZverevaDanceWCS.service.model.lessons.lessonDTO;

import com.example.ZverevaDanceWCS.service.Constant;
import com.example.ZverevaDanceWCS.service.model.lessons.Lesson;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LessonTelegramDto { //time-date -trainer/studentName
    @JsonFormat(pattern = "dd-MM-yy HH:mm")
    LocalDateTime dateTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    Long lessonId;
    Boolean isTrainer;
    String trainerName;
    String studentName;

    public LessonTelegramDto(Lesson lesson, Boolean isTrainer) {
        this.dateTime = lesson.getStartTime();
        this.lessonId = lesson.getId();
        this.isTrainer = isTrainer;
        this.trainerName = lesson.getTrainer().getName();
        this.studentName = lesson.getStudent().getName();
    }

    @Override
    public String toString() {
        if (isTrainer) {
            return dateTime.format(Constant.formatterTimeFirst) + " with " + studentName + "\n";
        } else {
            return dateTime.format(Constant.formatterTimeFirst) + " with " + trainerName + "\n";
        }
    }
}
