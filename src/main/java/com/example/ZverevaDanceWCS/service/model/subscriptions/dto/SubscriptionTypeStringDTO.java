package com.example.ZverevaDanceWCS.service.model.subscriptions.dto;

import com.example.ZverevaDanceWCS.service.model.subscriptions.SubscriptionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionTypeStringDTO {
    Long SubscriptionTypeId;
    String SubscriptionTypeString;
    String info;

    public SubscriptionTypeStringDTO(SubscriptionType subscriptionType) {
        this.SubscriptionTypeId = subscriptionType.getId();
        this.SubscriptionTypeString = subscriptionType.toString();
        this.info=subscriptionType.getInfo();
    }
}
