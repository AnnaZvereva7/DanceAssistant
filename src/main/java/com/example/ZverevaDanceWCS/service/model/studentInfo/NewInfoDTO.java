package com.example.ZverevaDanceWCS.service.model.studentInfo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NewInfoDTO {
    @Positive
    public int studentId;
    @NotNull
    @NotBlank
    public String info;
}
