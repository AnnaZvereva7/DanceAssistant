package com.example.ZverevaDanceWCS.service.model.payments;

import com.example.ZverevaDanceWCS.service.model.exception.NotFoundException;
import com.example.ZverevaDanceWCS.service.model.subscriptions.StudentSubscription;
import com.example.ZverevaDanceWCS.service.model.subscriptions.SubscriptionService;
import com.example.ZverevaDanceWCS.service.model.subscriptions.SubscriptionStatus;
import com.example.ZverevaDanceWCS.service.model.user.User;
import com.example.ZverevaDanceWCS.service.model.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Objects;

@Service
public class PaymentService {

    final PaymentRepository paymentRepository;
    final UserService userService;
    final SubscriptionService subscriptionService;

    public PaymentService(PaymentRepository paymentRepository, UserService userService, SubscriptionService subscriptionService) {
        this.paymentRepository = paymentRepository;
        this.userService = userService;
        this.subscriptionService = subscriptionService;
    }

    public Payment saveNew(User student, Long trainerId, int sum, LocalDate date) throws RuntimeException {
        Payment newPayment = new Payment();
        newPayment.setStudent(student);
        newPayment.setTrainerId(trainerId);
        newPayment.setSum(sum);
        newPayment.setDate(date);
        return paymentRepository.save(newPayment);
    }

    public Payment update(Payment payment) {
        return paymentRepository.save(payment);
    }

    public Payment findById(Long paymentId) {
        if (paymentRepository.findById(paymentId).isPresent()) {
            return paymentRepository.findById(paymentId).get();
        } else {
            throw new NotFoundException("Payment with id=" + paymentId + " not found");
        }
    }

    public List<Payment> findByStudent(Long studentId) {
        return paymentRepository.findByStudentId(studentId);
    }

    public List<Payment> findByYearAndTrainerId(int year, Long trainerId) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);
        return paymentRepository.findByDateBetweenAndTrainerId(startDate, endDate, trainerId);
    }

    public List<Payment> findByMonthAndYearAndTrainerId(int month, int year, Long trainerId) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());
        return paymentRepository.findByDateBetweenAndTrainerId(startDate, endDate, trainerId);
    }

    public List<Payment> findByStudentAndPeriod(Long studentId, LocalDate startDate, LocalDate endDate) {
        return paymentRepository.findByStudentIdAndDateBetween(studentId, startDate, endDate);
    }

    @Transactional
    public void markSubscriptionPaid(Long subscriptionId, Long trainerId) {
        StudentSubscription subscription = subscriptionService.findById(subscriptionId);
        if(!Objects.equals(subscription.getSubscriptionType().getTrainerId(), trainerId)) {
            throw new IllegalArgumentException("You are not authorized to mark this subscription as paid");
        }
        Payment payment = new Payment();
        payment.setStudent(userService.findById(subscription.getStudentId()));
        payment.setTrainerId(subscription.getSubscriptionType().getTrainerId());
        payment.setSum(subscription.getSubscriptionType().getPrice());
        payment.setDate(LocalDate.now());
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscriptionService.saveSubscription(subscription);
        paymentRepository.save(payment);
    }

}
