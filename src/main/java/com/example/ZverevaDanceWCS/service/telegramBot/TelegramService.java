package com.example.ZverevaDanceWCS.service.telegramBot;

import com.example.ZverevaDanceWCS.service.Constant;
import com.example.ZverevaDanceWCS.service.googleCalendar.GoogleCalendarService;
import com.example.ZverevaDanceWCS.service.model.exception.CommandNotRecognizedException;
import com.example.ZverevaDanceWCS.service.model.exception.WrongDateException;
import com.example.ZverevaDanceWCS.service.model.lessons.Lesson;
import com.example.ZverevaDanceWCS.service.model.lessons.LessonService;
import com.example.ZverevaDanceWCS.service.model.lessons.LessonStatus;
import com.example.ZverevaDanceWCS.service.model.lessons.lessonDTO.LessonTelegramDto;
import com.example.ZverevaDanceWCS.service.model.user.studentInfo.InfoService;
import com.example.ZverevaDanceWCS.service.model.user.User;
import com.example.ZverevaDanceWCS.service.model.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
public class TelegramService {

    final UserService userService;
    final LessonService lessonService;
    final GoogleCalendarService calendarService;
    final InfoService infoService;

    public TelegramService(UserService userService, LessonService lessonService, GoogleCalendarService calendarService,
                           InfoService infoService) {
        this.userService = userService;
        this.lessonService = lessonService;
        this.calendarService = calendarService;
        this.infoService = infoService;
    }

    public String startCommandRecieveUser( String name) {
        return  "Hi, " + name + "!";
    }

    public String printMenuUser() {
        StringBuilder menu = new StringBuilder("You can use one of these commands: \n");
        menu.append("to see actual information about lessons-> \n " + "/show_info\n");
       return menu.toString();
    }

    public String schedule(long chatId) {
        User user = userService.findByChatId(chatId);
        List<LessonTelegramDto> lessonsStudent = lessonService.findByStudentAndDateAfter(user.getId(), LocalDateTime.now())
                .stream()
                .filter(lesson -> lesson.getStatus() != LessonStatus.CANCELED)
                .map(lesson -> new LessonTelegramDto(lesson, false))
                .toList();
        List<LessonTelegramDto> lessonsTrainer = lessonService.findByStatusInAndTrainerId(
                List.of(LessonStatus.PLANNED, LessonStatus.PENDING_TRAINER_CONFIRMATION, LessonStatus.PENDING_STUDENT_CONFIRMATION),
                user.getId())
                .stream()
                .map(lesson -> new LessonTelegramDto(lesson, true))
                .toList();
        if (lessonsStudent.isEmpty()&&lessonsTrainer.isEmpty()) {
            return  "No planned lessons";
        } else {
            return Stream.concat(lessonsStudent.stream(), lessonsTrainer.stream())
                    .sorted(Comparator.comparing(LessonTelegramDto::getDateTime))
                    .map(LessonTelegramDto::toString)
            .collect(Collectors.joining("\n"));
        }
    }

    public String showInfo(Long chatId) {
        User user = userService.findByChatId(chatId);
        return infoService.findByStudentActual(user.getId());
    }

}
