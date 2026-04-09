package com.example.ZverevaDanceWCS.service.telegramBot;

import com.example.ZverevaDanceWCS.service.Constant;
import com.example.ZverevaDanceWCS.service.model.lessons.LessonService;
import com.example.ZverevaDanceWCS.service.model.lessons.Lesson;
import com.example.ZverevaDanceWCS.service.model.lessons.LessonStatus;
import com.example.ZverevaDanceWCS.service.model.user.Messenger;
import com.example.ZverevaDanceWCS.service.model.user.User;
import com.example.ZverevaDanceWCS.service.model.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TelegramScheduledBot {

    final TelegramBot telegramBot;

    final UserService userService;
    final LessonService lessonService;

    public TelegramScheduledBot(TelegramBot telegramBot, UserService userService, LessonService lessonService) {
        this.telegramBot = telegramBot;
        this.userService = userService;
        this.lessonService = lessonService;
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
            for(Lesson lesson: lessons) {
                if (lesson.getTrainer().getMessenger()==Messenger.TELEGRAM) {
                    telegramBot.send(lesson.getTrainer().getChatId(), "Tomorrow lesson" + lesson.getStartTime().format(Constant.formatterDayTime) + " with " + lesson.getStudent().getName());
                }
                if (lesson.getStudent().getMessenger() == Messenger.TELEGRAM) {
                    telegramBot.send(lesson.getStudent().getChatId(), "Tomorrow lesson at " + lesson.getStartTime().format(Constant.formatterDayTime));
                }
            }

        } else {
            log.info("no lessons tomorrow");
        }
    }

    @Scheduled(cron = "0 0 8 * * *")
    public void passedLessons() { //09.05.25 Anton NEW (lessonId=12)
        List<Lesson> lessons = lessonService.findPassedNotCompletedLessons();
        if(!lessons.isEmpty()) {
            for(Lesson lesson : lessons) {
                if (lesson.getTrainer().getMessenger() == Messenger.TELEGRAM) {
                    telegramBot.send(lesson.getTrainer().getChatId(), "Lesson " + lesson.getStartTime().format(Constant.formatterJustDate) + " with " + lesson.getStudent().getName() + " is not completed");
                }
            }
        } else {
            log.info("All lessons completed");
        }

    }

}
