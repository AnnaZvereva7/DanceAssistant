package com.example.ZverevaDanceWCS.service.model.mailService;

import com.example.ZverevaDanceWCS.service.model.lessons.Lesson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class CalendarIcsBuilder {
    private static final ZoneId LESSON_ZONE =
            ZoneId.of("Europe/Amsterdam");
    private static final DateTimeFormatter UTC_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    @Value("${uid.domen}")
    private String uidDomen;

    public String buildRequest(Lesson lesson, int sequence) {
        return buildCalendar(lesson, sequence, "REQUEST", false);
    }

    public String buildCancel(Lesson lesson, int sequence) {
        return buildCalendar(lesson, sequence, "CANCEL", true);
    }

    private String buildCalendar(
            Lesson lesson,
            int sequence,
            String method,
            boolean cancelled
    ) {
        String uid = buildUid(lesson.getId());
        String ORGANIZER_NAME = lesson.getTrainer().getName();
        String ORGANIZER_EMAIL=lesson.getTrainer().getEmail();

        String dtStamp = formatUtc(Instant.now());

        String start = formatUtc(
                lesson.getStartTime()
                        .atZone(LESSON_ZONE)
                        .toInstant()
        );

        String end = formatUtc(
                lesson.getEndTime()
                        .atZone(LESSON_ZONE)
                        .toInstant()
        );

        StringBuilder ics = new StringBuilder();

        ics.append("BEGIN:VCALENDAR\r\n");
        ics.append("VERSION:2.0\r\n");
        ics.append("PRODID:-//DanceAssistant//Lessons//EN\r\n");
        ics.append("CALSCALE:GREGORIAN\r\n");
        ics.append("METHOD:").append(method).append("\r\n");

        ics.append("BEGIN:VEVENT\r\n");

        ics.append("UID:")
                .append(uid)
                .append("\r\n");

        ics.append("SEQUENCE:")
                .append(sequence)
                .append("\r\n");

        ics.append("DTSTAMP:")
                .append(dtStamp)
                .append("\r\n");

        ics.append("DTSTART:")
                .append(start)
                .append("\r\n");

        ics.append("DTEND:")
                .append(end)
                .append("\r\n");

        ics.append("SUMMARY:")
                .append(escape("West Coast Swing private lesson"))
                .append("\r\n");

        ics.append("ORGANIZER;CN=")
                .append(escapeParameter(ORGANIZER_NAME))
                .append(":mailto:")
                .append(ORGANIZER_EMAIL)
                .append("\r\n");

        if (lesson.getStudent() != null
                && lesson.getStudent().getEmail() != null) {

            ics.append("ATTENDEE;CN=")
                    .append(escapeParameter(lesson.getStudent().getName()))
                    .append(";RSVP=FALSE:mailto:")
                    .append(lesson.getStudent().getEmail())
                    .append("\r\n");
        }

        if (cancelled) {
            ics.append("STATUS:CANCELLED\r\n");
        }

        ics.append("END:VEVENT\r\n");
        ics.append("END:VCALENDAR\r\n");

        return ics.toString();
    }

    private String buildUid(Long lessonId) {
        return "lesson-" + lessonId + uidDomen;
    }

    private String formatUtc(Instant instant) {
        return UTC_FORMATTER
                .withZone(ZoneOffset.UTC)
                .format(instant);
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace("\r\n", "\\n")
                .replace("\n", "\\n");
    }

    private String escapeParameter(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

}
