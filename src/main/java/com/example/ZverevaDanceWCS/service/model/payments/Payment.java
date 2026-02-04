package com.example.ZverevaDanceWCS.service.model.payments;

import com.example.ZverevaDanceWCS.service.Constant;
import com.example.ZverevaDanceWCS.service.model.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "payments")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    int id;

    @ManyToOne
    @JoinColumn(name = "student_id", referencedColumnName = "user_id", nullable = false)
    User student;

    @Column (name = "trainer_id", nullable = false)
    int trainerId;

    @Column(name = "date_of_payment", nullable = false)
    LocalDate date;

    @Column(name = "amount", nullable = false)
    int sum;

    @Override
    public String toString() {
        return this.date.format(Constant.formatterJustDate)+" - "+this.sum +" EUR;";
    }
}
