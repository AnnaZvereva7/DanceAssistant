package com.example.ZverevaDanceWCS.service.model.subscriptions.dto;

import com.example.ZverevaDanceWCS.service.model.lessons.Lesson;
import com.example.ZverevaDanceWCS.service.model.lessons.lessonDTO.LessonShortDTO;
import com.example.ZverevaDanceWCS.service.model.subscriptions.StudentSubscription;
import com.example.ZverevaDanceWCS.service.model.subscriptions.SubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentSubscriptionFullDTO {
    Long id;
    String studentName;
    LocalDate startDate;
    LocalDate endDate; //todo changable
    int remainingLessons;
    SubscriptionStatus status; //todo changable
    String comment;
    List<LessonShortDTO> lessons; //todo changable - remove from list

    public StudentSubscriptionFullDTO (StudentSubscription subscription, List<Lesson> lessons) {
        this.id = subscription.getId();
        this.studentName = subscription.getStudentName();
        this.startDate = subscription.getStartDate();
        this.endDate = subscription.getEndDate();
        this.remainingLessons = subscription.getRemainingLessons();
        this.status = subscription.getStatus();
        this.comment = subscription.getComment();
        if (!lessons.isEmpty()) {
            this.lessons = lessons.stream().map(LessonShortDTO::toShortDTO).toList();
        } else {this.lessons=null;}
    }
}
