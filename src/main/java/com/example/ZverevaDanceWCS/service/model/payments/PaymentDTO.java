package com.example.ZverevaDanceWCS.service.model.payments;

import com.example.ZverevaDanceWCS.service.model.lessons.Lesson;
import com.example.ZverevaDanceWCS.service.model.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PaymentDTO {
    int studentId;
    String studentName;
    List<String> completedLessons;
    int balance;

    public PaymentDTO toPaymentDTO(User student, List<Lesson> lessons) {
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setStudentId(student.getId());
        paymentDTO.setStudentName(student.getName());
        paymentDTO.setCompletedLessons(lessons.stream().map(Lesson::stringForBill).toList());
        paymentDTO.setBalance(student.getBalance());
        return paymentDTO;
    }
}
