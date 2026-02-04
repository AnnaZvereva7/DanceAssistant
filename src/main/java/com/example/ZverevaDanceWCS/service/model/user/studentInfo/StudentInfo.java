package com.example.ZverevaDanceWCS.service.model.user.studentInfo;

import com.example.ZverevaDanceWCS.service.Constant;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Comparator;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "student_info")
public class StudentInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "info_id")
    int id;

    @Column(name = "student_id", nullable = false)
    int studentId;

    @Column (name="trainer_id", nullable = false)
    int trainerId;

    @Column(name="info_date", nullable = false)
    LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    InfoStatus status;

    @Column(nullable = false, columnDefinition = "TEXT")
    String info;

    public static Comparator<StudentInfo> byDate() {
        return Comparator.comparing(StudentInfo::getDate);
    }

    @Override
    public String toString () {
        return date.format(Constant.formatterJustDate)+ " ("+id+ ") "+info;
    }
}
