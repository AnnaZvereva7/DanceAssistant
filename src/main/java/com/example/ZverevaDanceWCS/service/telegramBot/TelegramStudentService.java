package com.example.ZverevaDanceWCS.service.telegramBot;

import com.example.ZverevaDanceWCS.service.Constant;
import com.example.ZverevaDanceWCS.service.googleCalendar.GoogleCalendarService;
import com.example.ZverevaDanceWCS.service.model.exception.CommandNotRecognizedException;
import com.example.ZverevaDanceWCS.service.model.exception.WrongDateException;
import com.example.ZverevaDanceWCS.service.model.lessons.Lesson;
import com.example.ZverevaDanceWCS.service.model.lessons.LessonService;
import com.example.ZverevaDanceWCS.service.model.lessons.LessonStatus;
import com.example.ZverevaDanceWCS.service.model.lessons.lessonDTO.LessonUserDTO;
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

@Slf4j
@Component
public class TelegramStudentService {

    final UserService userService;
    final LessonService lessonService;
    final GoogleCalendarService calendarService;
    final InfoService infoService;

    public TelegramStudentService(UserService userService, LessonService lessonService, GoogleCalendarService calendarService,
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
        //menu.append("to see list of unpaid lessons ->\n /unpaid\n"); //todo
       return menu.toString();
    }

    public String scheduleForStudent(long chatId) {
        User student = userService.findByChatId(chatId);
        List<Lesson> lessons = lessonService.findByStudentAndDateAfter(student.getId(), LocalDateTime.now());
        if (lessons == null) {
            return  "No planned lessons";
        } else {
           return lessons.stream()
                    .filter(lesson -> lesson.getStatus() != LessonStatus.CANCELED)
                    .sorted(Comparator.comparing(Lesson::getStartTime))
                    .map(LessonUserDTO::new)
                    .map(LessonUserDTO::toString)
                    .collect(Collectors.joining("\n"));
        }
    }

    public HashMap<Long, String> cancelLessonByStudent(String[] addInfo, long chatId) { //cancel_lesson:01.01.25
        User student = userService.findByChatId(chatId);
        HashMap<Long, String> responses = new HashMap<>();
        if (addInfo == null) { //cancel all future lessons
            List<Lesson> lessons
                    = lessonService.findByStudentAndDateAfter(student.getId(), LocalDateTime.now());
            for (Lesson lesson : lessons) {
                lessonService.cancelLesson(lesson);
            }
            responses.put(chatId, "All future lessons CANCELED");
            responses.put(Constant.adminChatId, "Student " + student.getName() + " canceled all future lessons");
            //todo notification to trainer instead of admin, if lessons with different trainers, them to all of them
        } else if (addInfo.length == 1) {
            LocalDate date = LocalDate.parse(addInfo[0], Constant.formatterJustDate);
            if (date.isBefore(LocalDate.now())) {
                throw new WrongDateException("You can't cancel lesson in the past");
            }
            Lesson lesson = lessonService.findByStudentDate(date, student.getId());
            lessonService.cancelLesson(lesson);
            responses.put(chatId, "Lesson on " + lesson.getStartTime().format(Constant.formatterTimeFirst) + " was canceled");
            responses.put(Constant.adminChatId, "Student " + student.getName() + " canceled lesson on " + lesson.getStartTime().format(Constant.formatterTimeFirst));
            //todo notification to trainer instead of admin
        } else {
            throw new CommandNotRecognizedException();
        }
        return responses;
    }

    public String showInfo(Long chatId) {
        User user = userService.findByChatId(chatId);
        return infoService.findByStudentActual(user.getId());
    }

}
