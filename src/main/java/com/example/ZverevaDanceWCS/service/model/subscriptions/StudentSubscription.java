package com.example.ZverevaDanceWCS.service.model.subscriptions;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;

@Entity
@Table(name = "students_subscriptions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StudentSubscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_subscription_id")
    Long id;

    @ManyToOne
    @JoinColumn(name = "subscription_type_id", referencedColumnName = "subscription_type_id")
    SubscriptionType subscriptionType;

    @Column (name = "student_id", nullable = false)
    Long studentId;

    @Column (name = "student_name", nullable = false)
    String studentName;

    @Column (name="trainer_name", nullable = false)
    String trainerName;

    @Column(name = "start_date", nullable = false)
    LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    LocalDate endDate;

    @Column(name = "remaining_lessons", nullable = false)
    int remainingLessons;

    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    SubscriptionStatus status;

    @Column
    String comment;

    public static final Comparator<StudentSubscription> BY_STATUS =
            Comparator.comparingInt(subscription ->
                    switch (subscription.getStatus()) {
                        case UNPAID -> 0;
                        case ACTIVE -> 1;
                        case EXPIRED -> 2;
                    }
            );

}
