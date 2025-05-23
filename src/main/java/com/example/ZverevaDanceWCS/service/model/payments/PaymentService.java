package com.example.ZverevaDanceWCS.service.model.payments;

import com.example.ZverevaDanceWCS.service.model.exception.NotFoundException;
import com.example.ZverevaDanceWCS.service.model.user.User;
import com.example.ZverevaDanceWCS.service.model.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PaymentService {

    @Autowired
    public final PaymentRepository paymentRepository;
    @Autowired
    public final UserService userService;

    public PaymentService(PaymentRepository paymentRepository, UserService userService) {
        this.paymentRepository = paymentRepository;
        this.userService = userService;
    }

    public Payment saveNew(User student, int sum, LocalDate date) throws RuntimeException {
        Payment newPayment = new Payment();
        newPayment.setStudent(student);
        newPayment.setSum(sum);
        newPayment.setDate(date);
        return paymentRepository.save(newPayment);
    }

    public Payment update (Payment payment) {
        return paymentRepository.save(payment);
    }

    public Payment findById (int paymentId) {
        if(paymentRepository.findById(paymentId).isPresent()) {
            return  paymentRepository.findById(paymentId).get();
        } else {
            throw new NotFoundException("Payment with id="+paymentId+" not found");
        }
    }

    public List<Payment> findByStudent (int studentId) {
        return paymentRepository.findByStudentId(studentId);
    }

}
