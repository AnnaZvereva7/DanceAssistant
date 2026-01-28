package com.example.ZverevaDanceWCS.service.telegramBot;

import com.example.ZverevaDanceWCS.service.calendar.GoogleCalendarService;
import com.example.ZverevaDanceWCS.service.Constant;
import com.example.ZverevaDanceWCS.service.model.exception.CommandNotRecognizedException;
import com.example.ZverevaDanceWCS.service.model.exception.NotFoundException;
import com.example.ZverevaDanceWCS.service.model.exception.WrongDateException;
import com.example.ZverevaDanceWCS.service.model.lessons.*;
import com.example.ZverevaDanceWCS.service.model.lessons.lessonDTO.LessonUserDAO;
import com.example.ZverevaDanceWCS.service.model.user.User;
import com.example.ZverevaDanceWCS.service.model.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TelegramBotUser {
    @Autowired
    private final UserService userService;
    private final LessonService lessonService;
    private final GoogleCalendarService calendarService;

    public TelegramBotUser(UserService userService, LessonService lessonService, GoogleCalendarService calendarService) {
        this.userService = userService;
        this.lessonService = lessonService;
        this.calendarService = calendarService;
    }


    public HashMap<Long, String> startCommandRecieveUser(long chatId, String name, String chatName) {
        HashMap<Long, String> response = new HashMap<>();
        try{
            User user = userService.findByChatId(chatId);
            response.put(chatId, "Hi, " + name + "!");
        } catch (RuntimeException e) {
            User user = userService.newUserTelegram(chatId, name, chatName);
                response.put(chatId, "Hi, " + name + "!");
                response.put(Constant.adminChatId, "New user. id: "+user.getId()+" name: "+user.getName() +" chatName: "+user.getChatName());
        }
        return response;
    }

    public String printMenuUser() {
        StringBuilder menu = new StringBuilder("You can use one of these commands: \n");
        menu.append("to add new lesson -> \n git\n");
        menu.append("to change lesson day or time ->\n /change_lesson:01.01.25 to 02.01.01 09:30\n");
        menu.append("to cancel one lesson -> \n /cancel_lesson:01.01.25\n");
        menu.append("to cancel all lessons -> \n /cancel_lesson\n");
        menu.append("to see the list of all lessons in period -> \n " +
                "/lessons_in_period:01.01.25 [31.05.25]\n");
        //menu.append() //todo last_lessons:3
        //menu.append("to see list of unpaid lessons ->\n /unpaid\n"); //todo
        return menu.toString();
    }

    public String lessonsInPeriod (String[] addInfo, long chatId) { //lessons_in_period:01.01.25 [31.05.25]
        User student = userService.findByChatId(chatId);
        if(addInfo!=null && addInfo.length==2) {
            List<Lesson> lessons = lessonService.findByStudentAndPeriod(student.getId(),
                    LocalDate.parse(addInfo[0], Constant.formatterJustDate),
                    LocalDate.parse(addInfo[1], Constant.formatterJustDate));
            return lessons.stream()
                    .filter(lesson -> lesson.getStatus()!=LessonStatus.CANCELED)
                    .map(LessonUserDAO::new)
                    .map(LessonUserDAO::toString)
                    .collect(Collectors.joining("\n"));
        } else if (addInfo!=null&&addInfo.length==1) {
            LocalDate date = LocalDate.parse(addInfo[0], Constant.formatterJustDate);
            if(date.isBefore(LocalDate.now())){
                return lessonService.findByStudentAndPeriod(student.getId(), date, LocalDate.now())
                        .stream()
                        .filter(lesson -> lesson.getStatus()!=LessonStatus.CANCELED)
                        .map(LessonUserDAO::new)
                        .map(LessonUserDAO::toString)
                        .collect(Collectors.joining("\n"));
            } else {
                return lessonService.findByStudentAndPeriod(student.getId(), LocalDate.now(), date)
                        .stream()
                        .filter(lesson -> lesson.getStatus()!=LessonStatus.CANCELED)
                        .map(LessonUserDAO::new)
                        .map(LessonUserDAO::toString)
                        .collect(Collectors.joining("\n"));
            }
        } else {
            throw new CommandNotRecognizedException(Constant.CNR);
        }
    }

    public String schedule(long chatId) {
        User student = userService.findByChatId(chatId);
        List<Lesson> lessons = lessonService.findByStudentAndDateAfter(student.getId(), LocalDateTime.now());
        if (lessons == null) {
            return "No planned lessons";
        } else {
            return lessons.stream()
                    .filter(lesson -> lesson.getStatus() != LessonStatus.CANCELED)
                    .sorted(Comparator.comparing(Lesson::getStartTime))
                    .map(LessonUserDAO::new)
                    .map(LessonUserDAO::toString)
                    .collect(Collectors.joining("\n"));
        }
    }

    public String cancelLesson(String[] addInfo, long chatId) { //cancel_lesson:01.01.25
        String answer = "";
        User student = userService.findByChatId(chatId);
        if (addInfo == null) {
            List<Lesson> lessons = lessonService.findByStudentAndDateAfter(student.getId(), LocalDateTime.now());
            for (Lesson lesson : lessons) {
                lessonService.cancelLesson(lesson);            }
            //todo добавить инфо о том что больше не добавлять уроки в расписание?
            //todo  добавить расчет за месяц?
            answer = "All future lessons CANCELED";
        } else if (addInfo.length == 1) {
            LocalDate date = LocalDate.parse(addInfo[0], Constant.formatterJustDate);
            if (date.isBefore(LocalDate.now())) {
                throw new WrongDateException("You can't chancel lesson in the past");
            }
            Lesson lesson = lessonService.findByStudentDate(date, student.getId());
            lessonService.cancelLesson(lesson);
            answer = "Lesson on " + lesson.getStartTime().format(Constant.formatterTimeFirst) + " was canceled";
        } else {
            throw new CommandNotRecognizedException(Constant.CNR);
        }
        return answer;
    }

    @Transactional
    public String changeLesson(String[] addInfo, long chatId) throws RuntimeException { //WrongDate, UserNotFound, CommandNotRecognized, LessonNotFound
        String answer = "";
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
                answer = "Lesson changed, new time: " + lesson.getStartTime().format(Constant.formatterTimeFirst);
            }
        } else {
            throw new CommandNotRecognizedException(Constant.CNR);
        }
        return answer;
    }

    @Transactional
    public String addLesson(String[] addInfo, long chatId) {
        String answer = "";
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
            answer = "New lesson " + dateTime.format(Constant.formatterTimeFirst) + " successfully added";
        } else {
            throw new CommandNotRecognizedException(Constant.CNR);
        }
        return answer;
    }

}
