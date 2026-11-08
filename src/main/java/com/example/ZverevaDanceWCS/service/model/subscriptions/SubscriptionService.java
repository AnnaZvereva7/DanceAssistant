package com.example.ZverevaDanceWCS.service.model.subscriptions;


import com.example.ZverevaDanceWCS.service.model.lessons.Lesson;
import com.example.ZverevaDanceWCS.service.model.mailService.SubscriptionMailService;
import com.example.ZverevaDanceWCS.service.model.subscriptions.dto.ChangeSubscriptionDTO;
import com.example.ZverevaDanceWCS.service.telegramBot.TelegramBot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    final LessonSubscriptionLinkRepository lessonSubscriptionLinkRepository;
    final StudentSubscriptionRepository studentSubscriptionRepository;
    final SubscriptionMailService subscriptionMailService;
    final TelegramBot telegramBot;

    public List<StudentSubscription> getSubscriptionsByStudentId(Long studentId) {
        return studentSubscriptionRepository.findByStudentId(studentId);
    }

    public StudentSubscription saveSubscription(StudentSubscription subscription)  {
        if (subscription.getRemainingLessons() == 0) {
            subscription.setStatus(SubscriptionStatus.EXPIRED);
            subscriptionMailService.sendSubscriptionExpired(subscription);
        }
        return studentSubscriptionRepository.save(subscription);
    }

    public List<Lesson> getLessonsBySubscriptionId(Long subscriptionId) {
        return lessonSubscriptionLinkRepository.findBySubscriptionId(subscriptionId).stream()
                .map(LessonSubscriptionLink::getLesson)
                .toList();
    }

    public StudentSubscription StudentActiveSubscriptionOrNull(Long studentId, Long trainerId) {
        List<StudentSubscription> subscriptions = studentSubscriptionRepository.findByStudentIdAndStatus(studentId, SubscriptionStatus.ACTIVE);
        if (subscriptions.isEmpty()) {
            return null;
        } else {
            StudentSubscription result = null;
            for (StudentSubscription subscription : subscriptions) {
                if (subscription.getSubscriptionType().getTrainerId().equals(trainerId)) {
                    if (result != null) {
                        if (subscription.getStartDate().isBefore(result.getStartDate())) {
                            result = subscription;
                        }
                    } else
                        result = subscription;
                }
            }
            return result;
        }
    }

    public List<StudentSubscription> getSubscriptionByTrainerId(Long trainerId) {
        return studentSubscriptionRepository.findBySubscriptionTypeTrainerId(trainerId).stream()
                .sorted(StudentSubscription.BY_STATUS)
                .toList();
    }

    public StudentSubscription findById(Long subscriptionId) {
        return studentSubscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));
    }

    public void newLinkCompletedLesson(Lesson lesson, StudentSubscription subscription) {
        LessonSubscriptionLink link = new LessonSubscriptionLink();
        link.setLesson(lesson);
        link.setSubscription(subscription);
        lessonSubscriptionLinkRepository.save(link);
        subscription.setRemainingLessons(subscription.getRemainingLessons() - 1);
        saveSubscription(subscription);
    }

    public void deleteSubscription(Long subscriptionId, Long trainerId) {
        StudentSubscription subscription = findById(subscriptionId);
        if (!subscription.getSubscriptionType().getTrainerId().equals(trainerId)) {
            throw new IllegalArgumentException("You are not authorized to delete this subscription");
        }
        List<LessonSubscriptionLink> links = lessonSubscriptionLinkRepository.findBySubscriptionId(subscriptionId);
        if (!links.isEmpty()) {
            throw new RuntimeException("Cannot delete subscription with completed lessons");
        }
        if (subscription.getStatus() == SubscriptionStatus.ACTIVE) {
            throw new RuntimeException("Cannot delete active subscription");
        }
        studentSubscriptionRepository.delete(subscription);
    }

    public List<StudentSubscription> getSubscriptionByTrainerIdAndStatus(Long trainerId, SubscriptionStatus status) {
        return studentSubscriptionRepository.findBySubscriptionTypeTrainerIdAndStatus(trainerId, status);
    }

    public List<StudentSubscription> getSubscriptionByTrainerIdAndStudentIdAndStatusIn(Long trainerId, Long studentId, List<SubscriptionStatus> statuses) {
        return studentSubscriptionRepository.findBySubscriptionTypeTrainerIdAndStudentIdAndStatusIn(trainerId, studentId, statuses);
    }

    public StudentSubscription changeSubscription(Long id, ChangeSubscriptionDTO changeDto, Long trainerId) {
        StudentSubscription subscription = findById(id);
        if (!subscription.getSubscriptionType().getTrainerId().equals(trainerId)) {
            throw new IllegalArgumentException("You are not authorized to change this subscription");
        }
        if (changeDto.getEndDate() != null) {
            subscription.setEndDate(changeDto.getEndDate());
        }
        if (changeDto.getStatus() != null) {
            subscription.setStatus(changeDto.getStatus());
        }
        if (changeDto.getComment() != null) {
            subscription.setComment(changeDto.getComment());
        }
        return saveSubscription(subscription);
    }
}
