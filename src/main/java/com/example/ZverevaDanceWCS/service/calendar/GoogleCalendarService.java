package com.example.ZverevaDanceWCS.service.calendar;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;

@Service
public class GoogleCalendarService {
    @Value("${google.calendar.service-account.key-file}")
    private Resource serviceAccountResource;

    @Value("${google.calendar.teacher-calendar-id}")
    private String teacherCalendarId;

    @Value("${google.calendar.application-name}")
    private String applicationName;

    private Calendar calendar;


    private void initGoogleCalendar() throws Exception {
        if (calendar == null) {
            try (InputStream inputStream = serviceAccountResource.getInputStream()) {
                GoogleCredential credential = GoogleCredential
                        .fromStream(inputStream)
                        .createScoped(Collections.singleton(CalendarScopes.CALENDAR));

                calendar = new Calendar.Builder(
                        GoogleNetHttpTransport.newTrustedTransport(),
                        JacksonFactory.getDefaultInstance(),
                        credential
                ).setApplicationName(applicationName).build();
            }
        }
    }

    public String addEvent(String summary, String description, LocalDateTime start, LocalDateTime end) {
        try {
            initGoogleCalendar();

            Event event = new Event()
                    .setSummary(summary)
                    .setDescription(description);

            // Время начала
            EventDateTime startTime = new EventDateTime()
                    .setDateTime(new DateTime(makeZonedTime(start).toInstant().toString()))
                    .setTimeZone(makeZonedTime(start).getZone().toString());

            // Время окончания
            EventDateTime endTime = new EventDateTime()
                    .setDateTime(new DateTime(makeZonedTime(end).toInstant().toString()))
                    .setTimeZone(makeZonedTime(end).getZone().toString());

            event.setStart(startTime);
            event.setEnd(endTime);

            Event createdEvent = calendar.events().insert(teacherCalendarId, event).execute();

            return createdEvent.getId(); // id события в календаре
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при добавлении в календарь");
        }
    }

    public void updateEvent(String eventId, String newTitle, String newDescription,
                            LocalDateTime newStart, LocalDateTime newEnd) {
        try {
            initGoogleCalendar(); // если ещё не инициализирован
            Event event = calendar.events().get(teacherCalendarId, eventId).execute();
            if (newTitle != null) {
                event.setSummary(newTitle);
            }
            if (newDescription != null) {
                event.setDescription(newDescription);
            }
            if (newStart != null) {
                event.setStart(new EventDateTime()
                        .setDateTime(new DateTime(makeZonedTime(newStart).toInstant().toString()))
                        .setTimeZone(makeZonedTime(newStart).getZone().toString()));

                event.setEnd(new EventDateTime()
                        .setDateTime(new DateTime(makeZonedTime(newEnd).toInstant().toString()))
                        .setTimeZone(makeZonedTime(newEnd).getZone().toString()));
            }
            calendar.events().update(teacherCalendarId, eventId, event).execute();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при обновлении события в календаре");
        }

    }

    public void deleteEvent (String eventId) {
        try {
            initGoogleCalendar();
            calendar.events().delete(teacherCalendarId, eventId).execute();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при удалении в календаре");
        }
    }

    public ZonedDateTime makeZonedTime(LocalDateTime time) {
        ZoneId zoneId = ZoneId.of("Europe/Amsterdam");
        return time.atZone(zoneId);
    }

}
