package com.example.ZverevaDanceWCS.service.model.subscriptions.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentSubscriptionNewDTO {
    Long subscriptionTypeId;
    Long studentId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    //@FutureOrPresent
    LocalDate startDate;
}
