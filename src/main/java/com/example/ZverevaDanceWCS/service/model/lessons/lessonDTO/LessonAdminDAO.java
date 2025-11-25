package com.example.ZverevaDanceWCS.service.model.lessons.lessonDTO;

import com.example.ZverevaDanceWCS.service.Constant;
import com.example.ZverevaDanceWCS.service.model.lessons.Lesson;
import com.example.ZverevaDanceWCS.service.model.lessons.LessonStatus;
import lombok.AllArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@AllArgsConstructor
public class LessonAdminDAO { //09.30 22.04.25 - Anna - status:PLANNED (lesson_id)
    String studentName;
    LocalDateTime dateTime;
    LessonStatus status;
    int id;

    public LessonAdminDAO(Lesson lesson) {
        this.studentName = lesson.getStudent().getName();
        this.dateTime = lesson.getStartTime();
        this.status = lesson.getStatus();
        this.id = lesson.getId();
    }

    public String toString() {
        return dateTime.format(Constant.formatterTimeFirst) + " - " + studentName + " - status:" + status + " (" + id + ")";
    }
}
