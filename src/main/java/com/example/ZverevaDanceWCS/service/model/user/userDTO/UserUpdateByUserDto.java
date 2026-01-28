package com.example.ZverevaDanceWCS.service.model.user.userDTO;

import com.example.ZverevaDanceWCS.service.model.user.User;
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
    String schedule;
    String additionalInfo;

    public static UserUpdateByUserDto fromUser (User user) {
        UserUpdateByUserDto dto = new UserUpdateByUserDto();
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setBirthday(user.getBirthday());
        dto.setSchedule(user.toStringSchedule());
        dto.setAdditionalInfo(user.getAdditionalInfo());
        return dto;
    }
}
