package com.example.ZverevaDanceWCS.service.googleCalendar;

import com.example.ZverevaDanceWCS.service.model.lessons.Lesson;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class LessonMailService {

    private static final String FROM_EMAIL = "danceassistantnl@gmail.com";
    private static final ZoneId LESSON_ZONE = ZoneId.of("Europe/Amsterdam");
    private static final DateTimeFormatter ICS_UTC_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
    private final JavaMailSender mailSender;

    @Value("${uid.domen}")
    private String uidDomen;

    public LessonMailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendLessonCreatedEmail(Lesson lesson, String studentEmail) throws MessagingException {
        String uid = buildUid(lesson);
        log.info("метод по отправке письма запущен");
        int sequence = 0;

        String title = buildLessonTitle(lesson);


        String ics = buildCreateOrUpdateIcs(
                uid,
                sequence,
                title,
                lesson.getStartTime(),
                lesson.getEndTime()
        );

        String html = buildCreatedHtml(lesson);

        sendEmail(
                studentEmail,
                "Приглашение на урок: " + title,
                html,
                "lesson-invite.ics",
                ics,
                "text/calendar; charset=UTF-8; method=PUBLISH"
        );
    }

    public void sendLessonUpdatedEmail(Lesson lesson, String studentEmail, int sequence) throws MessagingException {
        String uid = buildUid(lesson);

        String title = buildLessonTitle(lesson);

        String ics = buildCreateOrUpdateIcs(
                uid,
                sequence,
                title,
                lesson.getStartTime(),
                lesson.getEndTime()
        );

        String html = buildUpdatedHtml(lesson);

        sendEmail(
                studentEmail,
                "Изменение урока: " + title,
                html,
                "lesson-updated.ics",
                ics,
                "text/calendar; charset=UTF-8; method=PUBLISH"
        );
    }

    public void sendLessonCancelledEmail(Lesson lesson, String studentEmail, int sequence) throws MessagingException {
        String uid = buildUid(lesson);

        String title = buildLessonTitle(lesson);

        String ics = buildCancelIcs(
                uid,
                sequence,
                title,
                lesson.getStartTime(),
                lesson.getEndTime()
        );

        String html = buildCancelledHtml(lesson);

        sendEmail(
                studentEmail,
                "Отмена урока: " + title,
                html,
                "lesson-cancelled.ics",
                ics,
                "text/calendar; charset=UTF-8; method=CANCEL"
        );
    }

    private void sendEmail(
            String to,
            String subject,
            String html,
            String attachmentName,
            String icsContent,
            String contentType
    ) throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper =
                new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

        helper.setFrom(FROM_EMAIL);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);

        helper.addAttachment(
                attachmentName,
                new ByteArrayResource(icsContent.getBytes(StandardCharsets.UTF_8)),
                contentType
        );

        mailSender.send(message);
    }

    private String buildCreateOrUpdateIcs(
            String uid,
            int sequence,
            String summary,
            LocalDateTime startLocal,
            LocalDateTime endLocal
    ) {
        return """
                BEGIN:VCALENDAR
                VERSION:2.0
                PRODID:-//Dance Assistant NL//Lessons//RU
                CALSCALE:GREGORIAN
                METHOD:PUBLISH
                BEGIN:VEVENT
                UID:%s
                DTSTAMP:%s
                DTSTART:%s
                DTEND:%s
                SEQUENCE:%d
                STATUS:CONFIRMED
                SUMMARY:%s
                END:VEVENT
                END:VCALENDAR
                """.formatted(
                escapeIcs(uid),
                nowUtc(),
                toUtcString(startLocal),
                toUtcString(endLocal),
                sequence,
                escapeIcs(summary)
        ).replace("\n", "\r\n");
    }

    private String buildCancelIcs(
            String uid,
            int sequence,
            String summary,
            LocalDateTime startLocal,
            LocalDateTime endLocal
    ) {
        return """
                BEGIN:VCALENDAR
                VERSION:2.0
                PRODID:-//Dance Assistant NL//Lessons//RU
                CALSCALE:GREGORIAN
                METHOD:CANCEL
                BEGIN:VEVENT
                UID:%s
                DTSTAMP:%s
                DTSTART:%s
                DTEND:%s
                SEQUENCE:%d
                STATUS:CANCELLED
                SUMMARY:%s
                END:VEVENT
                END:VCALENDAR
                """.formatted(
                escapeIcs(uid),
                nowUtc(),
                toUtcString(startLocal),
                toUtcString(endLocal),
                sequence,
                escapeIcs(summary)
        ).replace("\n", "\r\n");
    }

    private String buildCreatedHtml(Lesson lesson) {
        return """
                <html>
                  <body>
                    <h2>Новый урок</h2>
                    <p><b>Ученик:</b> %s</p>
                    <p><b>Тренер:</b> %s</p>
                    <p><b>Дата:</b> %s</p>
                    <p><b>Время:</b> %s - %s</p>
                    <p>
                      <a <b>lesson-invite.ics</b>
                         style="display:inline-block;padding:10px 16px;background:#1a73e8;color:#ffffff;text-decoration:none;border-radius:6px;">
                        Добавить в Google Календарь
                      </a>
                    </p>
                  </body>
                </html>
                """.formatted(
                escapeHtml(lesson.getStudent().getName()),
                escapeHtml(lesson.getTrainer().getName()),
                lesson.getStartTime().toLocalDate(),
                lesson.getStartTime().toLocalTime(),
                lesson.getEndTime().toLocalTime()
        );
    }

    private String buildUpdatedHtml(Lesson lesson) {
        return """
                <html>
                  <body>
                    <h2>Время урока изменено</h2>
                    <p><b>Ученик:</b> %s</p>
                    <p><b>Тренер:</b> %s</p>
                    <p><b>Новая дата:</b> %s</p>
                    <p><b>Новое время:</b> %s - %s</p>
                    <p>
                      <a <b>lesson-updated.ics</b>
                         style="display:inline-block;padding:10px 16px;background:#1a73e8;color:#ffffff;text-decoration:none;border-radius:6px;">
                        Обновить событие в Google Календаре
                      </a>
                    </p>
                  </body>
                </html>
                """.formatted(
                escapeHtml(lesson.getStudent().getName()),
                escapeHtml(lesson.getTrainer().getName()),
                lesson.getStartTime().toLocalDate(),
                lesson.getStartTime().toLocalTime(),
                lesson.getEndTime().toLocalTime()
        );
    }

    private String buildCancelledHtml(Lesson lesson) {
        return """
                <html>
                  <body>
                    <h2>Урок отменён</h2>
                    <p><b>Ученик:</b> %s</p>
                    <p><b>Тренер:</b> %s</p>
                    <p><b>Дата:</b> %s</p>
                    <p><b>Время:</b> %s - %s</p>
                    <p>Во вложении находится файл отмены события <b>lesson-cancelled.ics</b>.</p>
                  </body>
                </html>
                """.formatted(
                escapeHtml(lesson.getStudent().getName()),
                escapeHtml(lesson.getTrainer().getName()),
                lesson.getStartTime().toLocalDate(),
                lesson.getStartTime().toLocalTime(),
                lesson.getEndTime().toLocalTime()
        );
    }

    private String buildLessonTitle(Lesson lesson) {
        return "WCS lesson with " + lesson.getTrainer().getName();
    }

    private String buildUid(Lesson lesson) {
        return "lesson-" + lesson.getId() + "@" + uidDomen;
    }

    private String toUtcString(LocalDateTime localDateTime) {
        return localDateTime
                .atZone(LESSON_ZONE)
                .withZoneSameInstant(ZoneId.of("UTC"))
                .format(ICS_UTC_FORMAT);
    }

    private String nowUtc() {
        return Instant.now()
                .atZone(ZoneId.of("UTC"))
                .format(ICS_UTC_FORMAT);
    }


    private String escapeIcs(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace(",", "\\,")
                .replace(";", "\\;")
                .replace("\r\n", "\\n")
                .replace("\n", "\\n")
                .replace("\r", "\\n");
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

}