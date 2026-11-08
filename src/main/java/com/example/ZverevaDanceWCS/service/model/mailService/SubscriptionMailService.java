package com.example.ZverevaDanceWCS.service.model.mailService;

import com.example.ZverevaDanceWCS.service.Constant;
import com.example.ZverevaDanceWCS.service.model.subscriptions.StudentSubscription;
import com.example.ZverevaDanceWCS.service.model.user.User;
import com.example.ZverevaDanceWCS.service.model.user.UserService;
import com.example.ZverevaDanceWCS.service.telegramBot.TelegramBot;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubscriptionMailService {
    private final JavaMailSender mailSender;
    private final UserService userService;
    private final TelegramBot telegramBot;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendSubscriptionExpired(StudentSubscription subscription) {
        User student = userService.findById(subscription.getStudentId());
        String studentEmail = student.getEmail();
        if (studentEmail == null || studentEmail.isBlank()) {
            log.info("Subscription expiration email skipped for subscription {}: student has no email", subscription.getId());
            return;
        }

        MimeMessage message = mailSender.createMimeMessage();
        try {
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(studentEmail));
            message.setSubject("Subscription expired", StandardCharsets.UTF_8.name());
            message.setText("""
                    Hello %s,

                    Your dance lesson subscription with %s has expired.

                    Please contact your trainer or visit https://danceclasses.nl/ if you would like to renew it.
                    """.formatted(
                    student.getName(),
                    subscription.getTrainerName()
            ), StandardCharsets.UTF_8.name());
            mailSender.send(message);
            log.info("Subscription expiration email sent for subscription {}", subscription.getId());
            telegramBot.send(Constant.adminChatId, "Subscription with student " + student.getName()+" has expired");
        } catch (MessagingException e) {
            throw new IllegalStateException("Failed to send expiration email for subscription " + subscription.getId(), e);
        }
    }
}
