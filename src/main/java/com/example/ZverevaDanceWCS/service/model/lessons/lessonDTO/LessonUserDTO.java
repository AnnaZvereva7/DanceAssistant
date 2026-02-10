package com.example.ZverevaDanceWCS.service.model.lessons.lessonDTO;

import com.example.ZverevaDanceWCS.service.Constant;
import com.example.ZverevaDanceWCS.service.model.lessons.Lesson;
import com.example.ZverevaDanceWCS.service.model.lessons.LessonStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LessonUserDTO { //time date - status - recap
    @JsonFormat(pattern = "dd-MM-yy HH:mm")
    LocalDateTime dateTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    LessonStatus status;
    Long lessonId;

    public LessonUserDTO(Lesson lesson) {
        this.dateTime=lesson.getStartTime();
        this.status=lesson.getStatus();
        this.lessonId=lesson.getId();
    }

    @Override
    public String toString() {
        return dateTime.format(Constant.formatterTimeFirst) + "- "+status.toString()+"\n";
    }
}
