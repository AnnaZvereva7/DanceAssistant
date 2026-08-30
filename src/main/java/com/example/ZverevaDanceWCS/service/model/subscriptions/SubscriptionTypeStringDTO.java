package com.example.ZverevaDanceWCS.service.model.subscriptions;

public class SubscriptionTypeStringDTO {
    Long SubscriptionTypeId;
    String SubscriptionTypeString;

    public SubscriptionTypeStringDTO(SubscriptionType subscriptionType) {
        this.SubscriptionTypeId = subscriptionType.getId();
        this.SubscriptionTypeString = subscriptionType.toString();
    }
}
