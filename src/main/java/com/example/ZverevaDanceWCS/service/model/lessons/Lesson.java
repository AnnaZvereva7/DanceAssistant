package com.example.ZverevaDanceWCS.service.model.lessons;

import com.example.ZverevaDanceWCS.service.Constant;
import com.example.ZverevaDanceWCS.service.model.user.User;
import com.example.ZverevaDanceWCS.service.model.user.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "lessons")
public class Lesson implements Comparable<Lesson> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lesson_id")
    int id;

    @ManyToOne
    @JoinColumn(name = "student_id", referencedColumnName = "user_id")
    User student;

    @Column(name = "date_time", nullable = false)
    LocalDateTime startTime;

    @Enumerated(EnumType.STRING)
    LessonStatus status;

    @Column(name = "to_do")
    String recap;

    @Column
    int cost;

    @Column(name = "for_payment")
    int forPayment;

    @Column(name = "google_event_id")
    String googleEventId;

    @Column(name = "duration_min")
    int durationMin;

    @Column(name = "end_time")
    LocalDateTime endTime;

    @Column
    String title;
    //student_event_id

    public Lesson(User student, LocalDateTime startTime, LessonStatus status) {
        this.student = student;
        this.startTime = startTime;
        this.durationMin = 60;
        this.endTime = startTime.plusMinutes(durationMin);
        this.status = status;
        this.title = "WCS lesson " + student.getName();
    }

    public void setStartTime(LocalDateTime newDateTime) {
        this.startTime = newDateTime;
        this.endTime = startTime.plusMinutes(durationMin);
    }

    public void setCost() {
        if (this.student.getRole() == UserRole.PERMANENT) {
            this.cost = Constant.permanentStudentCost;
            this.forPayment = this.cost;
        } else {
            this.cost = Constant.newStudentCost;
            this.forPayment = this.cost;
        }
    }

    @Override
    public int compareTo(Lesson other) {
        return this.startTime.compareTo(other.startTime);
    }
}
