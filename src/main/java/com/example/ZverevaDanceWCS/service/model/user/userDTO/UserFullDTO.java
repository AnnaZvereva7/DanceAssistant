package com.example.ZverevaDanceWCS.service.model.user.userDTO;

import com.example.ZverevaDanceWCS.service.Constant;
import com.example.ZverevaDanceWCS.service.model.user.Language;
import com.example.ZverevaDanceWCS.service.model.user.Messenger;
import com.example.ZverevaDanceWCS.service.model.user.User;
import com.example.ZverevaDanceWCS.service.model.user.UserRole;
import com.example.ZverevaDanceWCS.service.model.user.schedule.Schedule;
import com.example.ZverevaDanceWCS.service.model.user.schedule.ScheduleShortDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserFullDTO {
    private int id;
    private String name;
    private String email;
    private UserRole role;
    private String plans;
    private String birthday;
    private Messenger messenger;
    private int balance;
    private Language language;
    private List<ScheduleShortDTO> scheduleDtos;

    public static UserFullDTO toFullDTO (User user, List<Schedule> schedules) {
        List<ScheduleShortDTO> scheduleDTOs=new ArrayList<>();
         if (!schedules.isEmpty()) {
                scheduleDTOs= schedules.stream().map(Schedule::toShortDto)
                .toList();
        }
        String birthdayString = user.getBirthday() != null
                ? user.getBirthday().format(Constant.formatterJustDate)
                : "";
        return new UserFullDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getAdditionalInfo(),
                birthdayString,
                user.getMessenger(),
                user.getBalance(),
                user.getLanguage(),
                scheduleDTOs
        );
    }
}
