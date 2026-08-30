package com.example.ZverevaDanceWCS.service.model.subscriptions;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionTypeRepository extends JpaRepository<SubscriptionType, Long> {
    List<SubscriptionType> findByTrainerId(Long trainerId);
}
