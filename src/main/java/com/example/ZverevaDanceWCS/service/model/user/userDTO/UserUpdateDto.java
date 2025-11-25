package com.example.ZverevaDanceWCS.service.model.user.userDTO;

import com.example.ZverevaDanceWCS.service.model.user.UserRole;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.scheduling.annotation.Scheduled;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDto {
    @NotNull
    @Positive
    int studentId;
    @NotNull
    @NotBlank
    String name;
    @Email
    String email;
    UserRole role;
    @NotNull
    @Pattern(
            regexp = "^(Not set|(MONDAY|TUESDAY|WEDNESDAY|THURSDAY|FRIDAY|SATURDAY|SUNDAY)\\s([01]\\d|2[0-3]):[0-5]\\d)$",
            message = "Time must be \"Not set\" or in format: DAYOFWEEK HH:mm"
    )
    String schedule;
}
