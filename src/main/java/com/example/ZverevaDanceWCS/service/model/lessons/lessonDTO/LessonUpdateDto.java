package com.example.ZverevaDanceWCS.service.model.lessons.lessonDTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LessonUpdateDto {
    @NotNull
    @Positive
    int lessonId;
    @NotNull
    @JsonFormat(pattern = "dd.MM.yy HH:mm")
    LocalDateTime startTime;
    @NotNull
    @Positive
    int durationInMinutes;
    @PositiveOrZero
    int cost;
}
