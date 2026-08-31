package com.example.ZverevaDanceWCS.service.model.subscriptions;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonSubscriptionLinkRepository extends JpaRepository<LessonSubscriptionLink, Long> {
    List<LessonSubscriptionLink> findBySubscriptionId(Long subscriptionId);
    List<LessonSubscriptionLink> findByLessonId(Long lessonId);
}
