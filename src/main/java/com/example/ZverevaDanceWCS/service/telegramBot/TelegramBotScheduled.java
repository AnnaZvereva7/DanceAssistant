package com.example.ZverevaDanceWCS.service.telegramBot;

import com.example.ZverevaDanceWCS.service.Constant;
import com.example.ZverevaDanceWCS.service.model.lessons.LessonService;
import com.example.ZverevaDanceWCS.service.model.lessons.Lesson;
import com.example.ZverevaDanceWCS.service.model.lessons.LessonStatus;
import com.example.ZverevaDanceWCS.service.model.user.Messenger;
import com.example.ZverevaDanceWCS.service.model.user.User;
import com.example.ZverevaDanceWCS.service.model.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
        lessons=lessons.stream().filter(lesson -> lesson.getStatus()!=LessonStatus.CANCELED).collect(Collectors.toList());
        for (Lesson lesson : lessons) {
            User student = lesson.getStudent();
            if (student.getMessenger() == Messenger.TELEGRAM) {
                bot.sendMessage(student.getChatId(), "Tomorrow lesson at " + lesson.getStartTime().format(Constant.timeFormatter));
                log.info("Отправлено уведомление о занятии " + student.getName());
            }
        }
    }

    @Scheduled(cron = "0 0 18 * * *")
    public void reminderAdmin() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<Lesson> lessons = lessonService.findByDate(tomorrow)
                .stream()
                .filter(lesson -> lesson.getStatus()!= LessonStatus.CANCELED)
                .sorted(Comparator.comparing(Lesson::getStartTime))
                .collect(Collectors.toList());
        if (!lessons.isEmpty()) {
            String answer = "Tomorrow lessons:\n";
            for (Lesson lesson : lessons) {
                answer = answer + lesson.getStartTime().format(Constant.formatterTimeFirst) + " - " + lesson.getStudent().getName() + " (lessonId=" + lesson.getId() + ")\n";
            }
            bot.sendMessage(Constant.adminChatId, answer);
            log.info("уведомление администратору о расписании на следующий день");
        } else {
            log.info("без уведомления администратору, занятий на следующий день нет");
        }
    }

    @Scheduled(cron = "0 0 8 * * *")
    public void passedLessons() { //09.05.25 Anton NEW (lessonId=12)
        List<Lesson> lessons = lessonService.findPassedNotCompletedLessons();
        if(!lessons.isEmpty()) {
            String answer = "Не завершенные занятия:\n";
            for (Lesson lesson : lessons) {
                answer = answer + lesson.getStartTime().format(Constant.formatterJustDate) + " " + lesson.getStudent().getName() + " " + lesson.getStatus() + " (lessonId=" + lesson.getId() + ")\n";
            }
            bot.sendMessage(Constant.adminChatId, answer);
            log.info("Отправлено уведомление о прошедших занятиях");
        } else {
            log.info("Нет незавершенных прошедших занятий");
        }

    }

//    @Scheduled(cron = "0 0 10 ? * SUN")
//    public void addScheduledLessons() {
//        List<User> students = userService.usersWithSchedule();
//        String response = "Scheduled lessons: \n";
//        for (User student : students) {
//            try {
//                Lesson lesson = lessonService.addLessonBySchedule(student);
//                if(student.getMessenger()==Messenger.TELEGRAM) {
//                    bot.sendMessage(student.getChatId(), "Scheduled lesson added:" + lesson.getStartTime().format(Constant.formatterTimeFirst));
//                }
//                response = response + student.getName() + " - " + lesson.getStartTime().format(Constant.formatterTimeFirst) + " (" + lesson.getId() + ")\n";
//            } catch (RuntimeException e) {
//                bot.sendMessage(Constant.adminChatId, e.getMessage());
//            }
//        }
//        bot.sendMessage(Constant.adminChatId, response);
//    }

}
