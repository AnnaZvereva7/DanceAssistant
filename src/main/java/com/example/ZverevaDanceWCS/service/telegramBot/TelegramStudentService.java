package com.example.ZverevaDanceWCS.service.telegramBot;

import com.example.ZverevaDanceWCS.service.Constant;
import com.example.ZverevaDanceWCS.service.calendar.GoogleCalendarService;
import com.example.ZverevaDanceWCS.service.model.exception.CommandNotRecognizedException;
import com.example.ZverevaDanceWCS.service.model.exception.NotFoundException;
import com.example.ZverevaDanceWCS.service.model.exception.WrongDateException;
import com.example.ZverevaDanceWCS.service.model.lessons.Lesson;
import com.example.ZverevaDanceWCS.service.model.lessons.LessonService;
import com.example.ZverevaDanceWCS.service.model.lessons.LessonStatus;
import com.example.ZverevaDanceWCS.service.model.lessons.lessonDTO.LessonUserDAO;
import com.example.ZverevaDanceWCS.service.model.studentInfo.InfoService;
import com.example.ZverevaDanceWCS.service.model.user.User;
import com.example.ZverevaDanceWCS.service.model.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public String startCommandRecieveUser(long chatId, String name) {
        User user = userService.findByChatId(chatId);
        return  "Hi, " + name + "!";
    }

    public String printMenuUser() {
        StringBuilder menu = new StringBuilder("You can use one of these commands: \n");
        menu.append("to add new lesson -> \n /new_lesson:01.01.25 09:30\n");
        menu.append("to change lesson day or time ->\n /change_lesson:01.01.25 to 02.01.01 09:30\n");
        menu.append("to cancel one lesson -> \n /cancel_lesson:[01.01.25]\n");
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
                    .map(LessonUserDAO::new)
                    .map(LessonUserDAO::toString)
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

    @Transactional
    public HashMap<Long, String> changeLessonByStudent(String[] addInfo, long chatId) throws RuntimeException { //WrongDate, UserNotFound, CommandNotRecognized, LessonNotFound
        HashMap<Long, String> responses = new HashMap<>();
        if (addInfo != null && addInfo.length == 4) {
            LocalDate oldDate = LocalDate.parse(addInfo[0], Constant.formatterJustDate);
            LocalDateTime newDate = LocalDateTime.parse(addInfo[2] + " " + addInfo[3], Constant.formatter);
            if (newDate.isBefore(LocalDateTime.now())) {
                throw new WrongDateException("New date can't be in the past");
            }
            User student = userService.findByChatId(chatId);
            Lesson lesson = lessonService.findByStudentDate(oldDate, student.getId());
            if (lesson.getStatus() != LessonStatus.NEW && lesson.getStatus() != LessonStatus.PLANNED) {
                throw new NotFoundException("You can't change lesson with status " + lesson.getStatus().toString());
            } else {
                lesson.setStartTime(newDate);
                lesson.setStatus(LessonStatus.NEW);
                lesson = lessonService.updateLesson(lesson);
                responses.put(chatId, "Lesson changed, new time: " + lesson.getStartTime().format(Constant.formatterTimeFirst));
                responses.put(Constant.adminChatId, "Student " + student.getName() + " changed lesson to " + lesson.getStartTime().format(Constant.formatterTimeFirst));
                //todo notification to trainer instead of admin
            }
        } else {
            throw new CommandNotRecognizedException();
        }
        return responses;
    }

    @Transactional
    public HashMap<Long, String> addLessonByStudent(String[] addInfo, long chatId) {
        HashMap<Long, String> responses = new HashMap<>();
        if (addInfo != null && addInfo.length == 2) {
            LocalDateTime dateTime = LocalDateTime.parse(addInfo[0] + " " + addInfo[1], Constant.formatter);
            if (dateTime.isBefore(LocalDateTime.now())) {
                throw new WrongDateException("Date of new lesson can't be in past");
            }
            User student = userService.findByChatId(chatId);
            if (lessonService.existByStudentDate(LocalDate.parse(addInfo[0], Constant.formatterJustDate), student.getId())) {
                throw new WrongDateException("You already have lesson at this date");
            }
            Lesson newLesson = new Lesson(student, dateTime, LessonStatus.NEW);
            Lesson lesson = lessonService.saveNewLesson(newLesson);
            responses.put(chatId, "New lesson " + dateTime.format(Constant.formatterTimeFirst) + " successfully added");
            responses.put(Constant.adminChatId, "Student " + student.getName() + " added new lesson on " + lesson.getStartTime().format(Constant.formatterTimeFirst));
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

    //TODO проверка даты и даты со временем, происходит только по дате, без учета времени
    private boolean checkDate(LocalDateTime dateTime, int deltaInDays) {
        return dateTime.isBefore(LocalDateTime.now().plusDays(deltaInDays));
    }
}
