package com.example.ZverevaDanceWCS.service.model.user.userDTO;

import com.example.ZverevaDanceWCS.service.model.user.User;
import com.example.ZverevaDanceWCS.service.model.user.schedule.ScheduleShortDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateByUserDto {
    @NotNull
    @NotBlank
    String name;

    @Email
    String email;

    @Past
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate birthday;
    List<ScheduleShortDTO> schedules;
    String additionalInfo;

    public static UserUpdateByUserDto fromUser (User user, List<ScheduleShortDTO> schedules) {
        UserUpdateByUserDto dto = new UserUpdateByUserDto();
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setBirthday(user.getBirthday());
        dto.setSchedules(schedules);
        dto.setAdditionalInfo(user.getAdditionalInfo());
        return dto;
    }
}
