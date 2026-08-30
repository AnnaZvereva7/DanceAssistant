package com.example.ZverevaDanceWCS.service.model.subscriptions;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubscriptionTypeService {
    private final SubscriptionTypeRepository subscriptionTypeRepository;

    public SubscriptionTypeService(SubscriptionTypeRepository subscriptionTypeRepository) {
        this.subscriptionTypeRepository = subscriptionTypeRepository;
    }

    public List<SubscriptionTypeStringDTO> getAllSubscriptionTypes(Long trainerId) {
        List<SubscriptionType> subscriptionTypes = subscriptionTypeRepository.findByTrainerId(trainerId);
        return subscriptionTypes.stream()
                .map(SubscriptionTypeStringDTO::new)
                .collect(Collectors.toList());
    }
}
