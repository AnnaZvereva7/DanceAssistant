package com.example.ZverevaDanceWCS.service.model.subscriptions.dto;

import com.example.ZverevaDanceWCS.service.model.subscriptions.SubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangeSubscriptionDTO {
    LocalDate endDate;
    SubscriptionStatus status;
    String comment;

}
