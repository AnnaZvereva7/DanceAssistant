package com.example.ZverevaDanceWCS.service.model.user.userDTO;

import com.example.ZverevaDanceWCS.service.model.user.UserRole;
import com.example.ZverevaDanceWCS.service.model.user.schedule.ScheduleShortDTO;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateByAdminDto {
    @NotNull
    @Positive
    int studentId;
    @NotNull
    @NotBlank
    String name;
    @Email
    String email;
    UserRole role;

    List<ScheduleShortDTO> scheduleDTOs;
}
