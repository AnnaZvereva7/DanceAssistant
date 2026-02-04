package com.example.ZverevaDanceWCS.service.model.user.schedule;

import com.example.ZverevaDanceWCS.service.Constant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;

@NoArgsConstructor
@Setter
@Getter
public class ScheduleShortDTO {
    int id;
    String dateTime;

    public Schedule fromShortDto(){
        Schedule schedule = new Schedule();
        schedule.setId(this.getId());
        String[] parts = this.getDateTime().split(" ");
        schedule.setScheduleDay(DayOfWeek.valueOf(parts[0]));
        schedule.setScheduleTime(LocalTime.parse(parts[1], Constant.formatterTime));
        return schedule;
    }
}