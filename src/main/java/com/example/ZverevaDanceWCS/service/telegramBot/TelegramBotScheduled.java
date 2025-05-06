package com.example.ZverevaDanceWCS.service.telegramBot;

import com.example.ZverevaDanceWCS.service.Constant;
import com.example.ZverevaDanceWCS.service.model.lessons.LessonService;
import com.example.ZverevaDanceWCS.service.model.lessons.Lesson;
import com.example.ZverevaDanceWCS.service.model.user.Messenger;
import com.example.ZverevaDanceWCS.service.model.user.User;
import com.example.ZverevaDanceWCS.service.model.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
public class TelegramBotScheduled {

    private final TelegramBot bot;

    @Autowired
    final UserService userService;
    @Autowired
    final LessonService lessonService;

    public TelegramBotScheduled(TelegramBot telegramBot, UserService userService, LessonService lessonService) {
        this.bot = telegramBot;
        this.userService = userService;
        this.lessonService = lessonService;
    }

    @Scheduled(cron = "0 0 18 * * *")
    public void reminder() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<Lesson> lessons = lessonService.findByDate(tomorrow);
        for (Lesson lesson : lessons) {
            User student = lesson.getStudent();
            if(student.getMessenger()== Messenger.TELEGRAM) {
                bot.sendMessage(student.getChatId(), "Tomorrow lesson at "+lesson.getDate().format(Constant.timeFormatter));
                log.info("Отправлено уведомление о занятии "+student.getName());
            }
        }
    }

    @Scheduled(cron = "0 0 9 * * *")
    public void passedLessons() {

        List<Lesson> lessons = lessonService.findPassedNotCompletedLessons();
        for (Lesson lesson : lessons) {
            User student = lesson.getStudent();
            if(student.getMessenger()== Messenger.TELEGRAM) {
                bot.sendMessage(student.getChatId(), "Tomorrow lesson at "+lesson.getDate().format(Constant.timeFormatter));
                log.info("Отправлено уведомление о занятии "+student.getName());
            }
        }
    }
    //по расписанию напоминание для меня отмечены ли прошедшие занятия выполненными (id-name-data)
    //по расписанию 1 числа каждого месяца список для оплат

}
