package com.example.ZverevaDanceWCS.service.model.lessons;

import com.example.ZverevaDanceWCS.service.Constant;
import lombok.AllArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Setter
public class LessonUserDAO { //time date - status - recap
    LocalDateTime dateTime;
    LessonStatus status;
    String recap;

    public LessonUserDAO (Lesson lesson) {
        this.dateTime=lesson.getDate();
        this.status=lesson.getStatus();
        this.recap=lesson.getRecap();
    }

    @Override
    public String toString() {
        return dateTime.format(Constant.formatterTimeFirst) + "- "+status.toString()+" recap: "+recap+"\n";
    }
}
