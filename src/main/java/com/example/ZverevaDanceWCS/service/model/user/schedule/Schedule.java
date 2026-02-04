package com.example.ZverevaDanceWCS.service.model.user.schedule;

import com.example.ZverevaDanceWCS.service.Constant;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "schedule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    int id;

    @Enumerated(EnumType.STRING)
    @Column(name="schedule_day")
    DayOfWeek scheduleDay;

    @Column(name = "schedule_time")
    LocalTime scheduleTime;

    @Column(name = "student_id", nullable = false)
    int studentId;

    @Column(name = "trainer_id", nullable = false)
    int trainerId;

    public String toStringSchedule () {
        return scheduleDay.toString()+" " + scheduleTime.format(Constant.formatterTime);
    }

    public ScheduleShortDTO toShortDto(){
        ScheduleShortDTO shortDTO = new ScheduleShortDTO();
        shortDTO.setId(this.id);
        shortDTO.setDateTime(toStringSchedule());
        return shortDTO;
    }


}
