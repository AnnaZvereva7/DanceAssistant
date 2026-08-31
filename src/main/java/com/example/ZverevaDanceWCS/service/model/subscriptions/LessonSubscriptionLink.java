package com.example.ZverevaDanceWCS.service.model.subscriptions;

import com.example.ZverevaDanceWCS.service.model.lessons.Lesson;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "lesson_subscription_links")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LessonSubscriptionLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lesson_subscription_link_id")
    Long id;

    @ManyToOne
    @JoinColumn(name = "lesson_id", referencedColumnName = "lesson_id")
    Lesson lesson;

    @ManyToOne
    @JoinColumn(name = "subscription_id", referencedColumnName = "student_subscription_id")
    StudentSubscription subscription;

}
