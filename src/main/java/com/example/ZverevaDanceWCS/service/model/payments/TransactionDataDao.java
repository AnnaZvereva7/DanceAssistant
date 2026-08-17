package com.example.ZverevaDanceWCS.service.model.payments;

import com.example.ZverevaDanceWCS.service.model.lessons.Lesson;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDataDao {
    Long id;
    LocalDate date;
    TransactionType type;
    String status;
    int amount;

    public static TransactionDataDao fromLesson(Lesson lesson) {
        TransactionDataDao dao = new TransactionDataDao();
        dao.id= lesson.getId();
        dao.date = lesson.getStartTime().toLocalDate();
        dao.type = TransactionType.LESSON;
        dao.status=lesson.getStatus().toString();
        dao.amount = -lesson.getCost();
        return dao;
    }

    public static TransactionDataDao fromPayment(Payment payment) {
        TransactionDataDao dao = new TransactionDataDao();
        dao.id=payment.getId();
        dao.date = payment.getDate();
        dao.type = TransactionType.PAYMENT;
        dao.status = " - ";
        dao.amount = payment.getSum();
        return dao;
    }
}
