package com.example.ZverevaDanceWCS.service.model.lessons.lessonDTO;

import com.example.ZverevaDanceWCS.service.Constant;
import com.example.ZverevaDanceWCS.service.model.lessons.Lesson;
import com.example.ZverevaDanceWCS.service.model.lessons.LessonStatus;
import lombok.AllArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Setter
public class LessonUserShortDAO { //time date - status
    LocalDateTime dateTime;
    LessonStatus status;

    public LessonUserShortDAO(Lesson lesson) {
        this.dateTime=lesson.getStartTime();
        this.status=lesson.getStatus();
    }

    @Override
    public String toString() {
        return dateTime.format(Constant.formatterTimeFirst) + "- "+status.toString()+"\n";
    }
}
