package com.example.ZverevaDanceWCS.service.model.lessons.lessonDTO;

import com.example.ZverevaDanceWCS.service.Constant;
import com.example.ZverevaDanceWCS.service.model.lessons.Lesson;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LessonShortDTO {
    private int id;
    private String studentName;
    private String startTime;

    public static LessonShortDTO toShortDTO(Lesson lesson) {
        LessonShortDTO dto = new LessonShortDTO();
        dto.id = lesson.getId();
        dto.studentName = lesson.getStudent().getName();
        dto.startTime = lesson.getStartTime().format(Constant.formatter);
        return dto;
    }
}
