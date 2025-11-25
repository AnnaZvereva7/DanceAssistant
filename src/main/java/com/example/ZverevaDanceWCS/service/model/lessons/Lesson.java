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
    int cost; //setCostAndForPayment() устанавливается на момент завершения урока как выполненный,
    // в зависимости от типа студената, для new = 40, permanent = 30, стоимость указана в Constant,
    //для групповых уроков стоимость указывается вручную при создании урока

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

    public Lesson(User student, LocalDateTime startTime, int duration, int cost) {
        this.student=student;
        this.startTime=startTime;
        this.durationMin=duration;
        this.endTime = startTime.plusMinutes(durationMin);
        this.status = LessonStatus.PLANNED;
        this.title =student.getName()+ " WCS lesson";
        this.cost=cost;
    }

    public Lesson(User student, LocalDateTime startTime, LessonStatus status) {
        this.student = student;
        this.startTime = startTime;
        this.durationMin = 60;
        this.endTime = startTime.plusMinutes(durationMin);
        this.status = status;
        this.title =student.getName()+ " WCS lesson";
    }

    public void setStartTime(LocalDateTime newDateTime) {
        this.startTime = newDateTime;
        this.endTime = startTime.plusMinutes(durationMin);
    }

    public void setCostAndForPayment() {
        if(this.getCost()==0) {
            if (this.student.getRole() == UserRole.PERMANENT) {
                this.cost = Constant.permanentStudentCost;
                this.forPayment = this.cost*durationMin/60;
            } else {
                this.cost = Constant.newStudentCost;
                this.forPayment = this.cost*durationMin/60;
            }
        } else {
            this.forPayment=this.cost;
        }

    }

    public void changeDuration(int duration) {
        int oldDuration=this.durationMin;
        this.durationMin=duration;
        this.endTime=startTime.plusMinutes(durationMin);
        if (this.status==LessonStatus.COMPLETED) {
            this.forPayment=this.cost*this.durationMin/oldDuration;
        } if (this.status==LessonStatus.PLANNED||this.status==LessonStatus.NEW) {
        if(this.forPayment!=0) {
        this.forPayment=this.cost*this.durationMin/60;}
        }
    }

    public String stringForBill() {
        return this.getStartTime().format(Constant.formatterJustDate) + " - " + this.getForPayment() + " EUR\n";
    }

    @Override
    public int compareTo(Lesson other) {
        return this.startTime.compareTo(other.startTime);
    }
}
