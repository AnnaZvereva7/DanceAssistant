package com.example.ZverevaDanceWCS.service.model.lessons.lessonDTO;

import com.example.ZverevaDanceWCS.service.model.lessons.Lesson;
import com.example.ZverevaDanceWCS.service.model.lessons.LessonStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LessonFullDTO {
    private Long id;
    private int studentId;
    private String studentName;
    private String startTime;
    private LessonStatus status;
    private int cost;
    private int forPayment;
    private int durationMin;
    private String title;

    public static LessonFullDTO toFullDTO(Lesson lesson) {
        LessonFullDTO dto = new LessonFullDTO();
        dto.id = lesson.getId();
        dto.studentName = lesson.getStudent().getName();
        dto.studentId=lesson.getStudent().getId();
        dto.startTime = lesson.getStartTime().format(com.example.ZverevaDanceWCS.service.Constant.formatterDayTime);
        dto.status = lesson.getStatus();
        dto.cost = lesson.getCost();
        dto.forPayment = lesson.getForPayment();
        dto.durationMin = lesson.getDurationMin();
        dto.title = lesson.getTitle();
        return dto;
    }
}
