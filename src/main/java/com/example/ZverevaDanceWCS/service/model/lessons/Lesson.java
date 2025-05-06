package com.example.ZverevaDanceWCS.service.model.lessons;

import com.example.ZverevaDanceWCS.service.Constant;
import com.example.ZverevaDanceWCS.service.model.user.User;
import com.example.ZverevaDanceWCS.service.model.user.UserRole;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "lessons")
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lesson_id")
    int id;

    @ManyToOne
    @JoinColumn(name = "student_id", referencedColumnName = "user_id")
    User student;

    @Column(name = "date_time", nullable = false)
    LocalDateTime date;

    @Enumerated(EnumType.STRING)
    LessonStatus status;

    @Column(name = "to_do")
    String recap;

    int cost;

    public void setCost () {
        if (this.student.getRole()==UserRole.PERMANENT) {
            this.cost= Constant.permanentStudentCost;
        } else {
            this.cost=Constant.newStudentCost;
        }
    }
}
