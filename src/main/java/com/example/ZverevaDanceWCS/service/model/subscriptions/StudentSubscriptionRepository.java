package com.example.ZverevaDanceWCS.service.model.subscriptions;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentSubscriptionRepository extends JpaRepository<StudentSubscription, Long> {
    List<StudentSubscription> findByStudentId(Long studentId);
    List<StudentSubscription> findByStudentIdAndStatus(Long studentId, SubscriptionStatus status);
    List<StudentSubscription> findBySubscriptionTypeTrainerId(Long trainerId);
    List<StudentSubscription> findBySubscriptionTypeTrainerIdAndStatus(Long trainerId, SubscriptionStatus status);
    List<StudentSubscription> findBySubscriptionTypeTrainerIdAndStudentIdAndStatusIn(Long trainerId, Long studentId, List<SubscriptionStatus> statuses);
}
