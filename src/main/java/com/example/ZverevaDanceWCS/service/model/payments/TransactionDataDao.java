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
    LocalDate date;
    TransactionType type;
    int amount;

    public static TransactionDataDao fromLesson(Lesson lesson) {
        TransactionDataDao dao = new TransactionDataDao();
        dao.date = lesson.getStartTime().toLocalDate();
        dao.type = TransactionType.LESSON;
        dao.amount = lesson.getForPayment();
        return dao;
    }

    public static TransactionDataDao fromPayment(Payment payment) {
        TransactionDataDao dao = new TransactionDataDao();
        dao.date = payment.getDate();
        dao.type = TransactionType.PAYMENT;
        dao.amount = payment.getSum();
        return dao;
    }
}
