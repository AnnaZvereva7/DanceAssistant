package com.example.ZverevaDanceWCS.service.model.subscriptions.dto;

import com.example.ZverevaDanceWCS.service.Constant;
import com.example.ZverevaDanceWCS.service.model.subscriptions.StudentSubscription;
import com.example.ZverevaDanceWCS.service.model.user.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentSubscriptionDTO {
    Long id;
    String info; //todo trainer name

    public StudentSubscriptionDTO(StudentSubscription subscription, UserRole role) {
        this.id = subscription.getId();
        if (role == UserRole.TRAINER||role == UserRole.ADMIN) {
            this.info = subscription.getStartDate().format(Constant.formatterJustDate) + " - " + subscription.getEndDate().format(Constant.formatterJustDate) +
                    "   remain lessons: " + subscription.getRemainingLessons() + "/" + subscription.getSubscriptionType().getAmountLessons() +
                    ", " + subscription.getStatus().toString() + "  (student: " + subscription.getStudentName() + ")";
        } else {
            this.info = subscription.getStartDate().format(Constant.formatterJustDate) + " - " + subscription.getEndDate().format(Constant.formatterJustDate) +
                    "   remain lessons: " + subscription.getRemainingLessons() + "/" + subscription.getSubscriptionType().getAmountLessons() +
                    ", " + subscription.getStatus().toString() + "  (trainer: " + subscription.getTrainerName() + ")";
        }
    }
}
