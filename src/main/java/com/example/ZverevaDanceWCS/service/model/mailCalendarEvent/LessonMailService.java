package com.example.ZverevaDanceWCS.service.model.mailCalendarEvent;

import com.example.ZverevaDanceWCS.service.Constant;
import com.example.ZverevaDanceWCS.service.model.lessons.Lesson;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@Slf4j
@RequiredArgsConstructor
public class LessonMailService {

    private final JavaMailSender mailSender;
    private final CalendarIcsBuilder calendarIcsBuilder;
    private final SequenceService sequenceService;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendLessonCreated(Lesson lesson) {
        int sequence = sequenceService.getOrCreate(lesson.getId());

        String ics = calendarIcsBuilder.buildRequest(
                lesson,
                sequence
        );
        send(
                lesson,
                "New lesson",
                """
                        A new lesson has been scheduled.
                        
                        Date and time: %s
                        """.formatted(
                        lesson.getStartTime().format(Constant.formatterDayTime)),
                ics,
                "REQUEST"
        );
    }

    private void send(
            Lesson lesson,
            String subject,
            String text,
            String ics,
            String method
    ) {
        String studentEmail = lesson.getStudent().getEmail();

        MimeMessage message = mailSender.createMimeMessage();

        try {
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(studentEmail)
            );
            message.setSubject(subject, StandardCharsets.UTF_8.name());

            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(
                    text,
                    StandardCharsets.UTF_8.name()
            );

            MimeBodyPart calendarPart = new MimeBodyPart();

            calendarPart.setContent(
                    ics,
                    "text/calendar; charset=UTF-8; method=" + method
            );

            calendarPart.setHeader(
                    "Content-Type",
                    "text/calendar; charset=UTF-8; method=" + method
            );

            calendarPart.setHeader(
                    "Content-Transfer-Encoding",
                    "8bit"
            );

            MimeMultipart multipart = new MimeMultipart("alternative");

            multipart.addBodyPart(textPart);
            multipart.addBodyPart(calendarPart);

            message.setContent(multipart);

            mailSender.send(message);

            log.info(
                    "Calendar email sent for lesson {} to {}",
                    lesson.getId(),
                    studentEmail
            );

        } catch (MessagingException e) {
            throw new IllegalStateException(
                    "Failed to send calendar email for lesson "
                            + lesson.getId(),
                    e
            );
        }
    }

}