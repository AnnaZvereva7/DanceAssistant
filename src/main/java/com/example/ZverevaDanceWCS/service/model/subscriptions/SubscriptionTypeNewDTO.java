package com.example.ZverevaDanceWCS.service.model.subscriptions;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SubscriptionTypeNewDTO {

    @NotNull
    @Positive
    int durationMonths;

    @NotNull
    @Positive
    int amountLessons;

    @NotNull
    @Positive
    int price;

}
