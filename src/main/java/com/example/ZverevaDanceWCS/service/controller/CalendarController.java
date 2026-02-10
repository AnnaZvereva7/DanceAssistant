package com.example.ZverevaDanceWCS.service.controller;

import com.example.ZverevaDanceWCS.service.Constant;
import com.example.ZverevaDanceWCS.service.model.calendarEvent.CalendarEventService;
import com.example.ZverevaDanceWCS.service.model.lessons.Lesson;
import com.example.ZverevaDanceWCS.service.model.lessons.LessonService;
import com.example.ZverevaDanceWCS.service.model.freeSlots.*;
import com.example.ZverevaDanceWCS.service.model.calendarEvent.CalendarEventDto;
import com.example.ZverevaDanceWCS.service.model.calendarEvent.TimeRequest;
import com.example.ZverevaDanceWCS.service.model.lessons.LessonStatus;
import com.example.ZverevaDanceWCS.service.model.trainerStudentLink.TrainerStudentService;
import com.example.ZverevaDanceWCS.service.model.user.Messenger;
import com.example.ZverevaDanceWCS.service.model.user.User;
import com.example.ZverevaDanceWCS.service.model.user.UserService;
import com.example.ZverevaDanceWCS.service.model.user.UserSiteStatus;
import com.example.ZverevaDanceWCS.service.model.user.userDTO.UserShortDTO;
import com.example.ZverevaDanceWCS.service.telegramBot.TelegramTrainerBot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/calendar")
@Slf4j
public class CalendarController {
 final FreeSlotService slotService;
 final CalendarEventService calendarEventService;
 final UserService userService;
 final LessonService lessonService;
 final TelegramTrainerBot telegramTrainerBot;
 final TrainerStudentService trainerStudentService;

    public CalendarController(FreeSlotService slotService, CalendarEventService calendarEventService, UserService userService, LessonService lessonService, TelegramTrainerBot telegramTrainerBot, TrainerStudentService trainerStudentService) {
        this.slotService = slotService;
        this.calendarEventService = calendarEventService;
        this.userService = userService;
        this.lessonService = lessonService;
        this.telegramTrainerBot = telegramTrainerBot;
        this.trainerStudentService = trainerStudentService;
    }

    private User findUserFromSession(HttpSession session) {
        int userId = (int) session.getAttribute("USER_ID");
        return userService.findById(userId);
    }

    @GetMapping("/role")
    public UserSiteStatus getUserRole(HttpSession session) {
        User user = findUserFromSession(session);
        return user.getUserSiteStatus();
    }

    @PreAuthorize("hasRole('TRAINER') or hasRole('ADMIN')")
    @PostMapping("/addFreeSlots")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addFreeSlots(@RequestBody TimeRequest timeRequest, HttpSession session) {
        User trainer =findUserFromSession(session);
        calendarEventService.addFreeSlot(timeRequest, trainer.getId());
    }

    @GetMapping
    @PreAuthorize("hasRole('TRAINER') or hasRole('ADMIN')")
    public List<CalendarEventDto> getCalendar(HttpSession session){
        User trainer = findUserFromSession(session);
        return calendarEventService.getTrainerCalendar(trainer.getId());
    }

    @GetMapping ("/user")
    @PreAuthorize("hasRole('USER')")
    public List<CalendarEventDto> getUserCalendar(HttpSession session){
        User student = findUserFromSession(session);
        return calendarEventService.getStudentLessons(student.getId());
    }

    @DeleteMapping ("/delete_slot/{slotId}")
    @PreAuthorize("hasRole('TRAINER') or hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFreeSlot(@PathVariable String slotId) {
        calendarEventService.deleteFreeSlot(slotId);
    }

   @PutMapping("/user/confirm_lesson/{lessonId}")
   @PreAuthorize("hasRole('USER')")
   @ResponseStatus(HttpStatus.NO_CONTENT)
    public void confirmLessonByStudent(@PathVariable(name = "lessonId") String eventId) {
        Long lessonId=calendarEventService.extractEventId(eventId, "lesson");
        Lesson lesson=lessonService.confirmLesson(lessonId, LessonStatus.PENDING_STUDENT_CONFIRMATION);
        if(lesson.getTrainer().getMessenger()== Messenger.TELEGRAM) {
            telegramTrainerBot.send(lesson.getTrainer().getChatId(), "Lesson confirmed: " + lesson.getStartTime().format(Constant.formatterDayTime) + " with student " + lesson.getStudent().getName());
        }
    }

    @PutMapping("/confirm_lesson/{lessonId}")
    @PreAuthorize("hasRole('TRAINER') or hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void confirmLessonByTrainer(@PathVariable(name = "lessonId") String eventId) {
        Long lessonId=calendarEventService.extractEventId(eventId, "lesson");
        Lesson lesson=lessonService.confirmLesson(lessonId, LessonStatus.PENDING_TRAINER_CONFIRMATION);
        if(lesson.getStudent().getMessenger()== Messenger.TELEGRAM) {
            telegramTrainerBot.send(lesson.getTrainer().getChatId(), "Lesson confirmed: " + lesson.getStartTime().format(Constant.formatterDayTime));
        }
    }

    @DeleteMapping("/user/delete_lesson/{lessonId}")
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLesson(@PathVariable(name = "lessonId") String eventId) {
        Long lessonId=calendarEventService.extractEventId(eventId, "lesson");
        Lesson lesson = lessonService.findById(lessonId);
        lessonService.cancelLesson(lesson);
        if(lesson.getTrainer().getMessenger()== Messenger.TELEGRAM) {
            telegramTrainerBot.send(lesson.getTrainer().getChatId(), "Lesson cancelled: " + lesson.getStartTime().format(Constant.formatterDayTime) + " with student " + lesson.getStudent().getName());
        }
    }

    @DeleteMapping("/delete_lesson/{lessonId}")
    @PreAuthorize("hasRole('TRAINER') or hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLessonByTrainerUnconfirmed(@PathVariable(name = "lessonId") String eventId) {
        Long lessonId=calendarEventService.extractEventId(eventId, "lesson");
        Lesson lesson = lessonService.findById(lessonId);
        lessonService.cancelLesson(lesson);
        if(lesson.getStudent().getMessenger()== Messenger.TELEGRAM) {
            telegramTrainerBot.send(lesson.getStudent().getChatId(), "Lesson cancelled: " + lesson.getStartTime().format(Constant.formatterDayTime));
        }
    }

    @GetMapping("user/get_free_slots/{trainerId}")
    @PreAuthorize("hasRole('USER')")
    public List<CalendarEventDto> getFreeSlotsByTrainerId(@PathVariable int trainerId) {
        return calendarEventService.getTrainerFreeSlots(trainerId);
    }

    @PostMapping("/user/new_lesson/{trainerId}")
    @PreAuthorize("hasRole('USER')")
    public CalendarEventDto addNewLesson(@PathVariable int trainerId, @RequestBody TimeRequest timeRequest, HttpSession session) {
        User student = findUserFromSession(session);
        User trainer = userService.findById(trainerId);
        Lesson lesson = lessonService.createLessonFromCalendar(timeRequest, trainer, student);
        if(lesson.getTrainer().getMessenger()== Messenger.TELEGRAM) {
            telegramTrainerBot.send(lesson.getTrainer().getChatId(), "New lesson: " + lesson.getStartTime().format(Constant.formatterDayTime) + " with student " + lesson.getStudent().getName());
        }
        return new CalendarEventDto(lesson);
    }

    @PutMapping("/user/change_lesson/{lessonId}")
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeLesson(@PathVariable(name = "lessonId") String eventId, @RequestBody TimeRequest timeRequest, HttpSession session) {
        Long lessonId = calendarEventService.extractEventId(eventId,"lesson");
        User student = findUserFromSession(session);
        Lesson lesson = lessonService.findById(lessonId);
        if (lesson.getStudent().getId()!= student.getId()) {
            throw new RuntimeException("You are not allowed to change this lesson");
        } else {
            if(lesson.getStatus()==LessonStatus.PENDING_STUDENT_CONFIRMATION|| lesson.getStatus()==LessonStatus.PLANNED
            || lesson.getStatus()==LessonStatus.PENDING_TRAINER_CONFIRMATION) {
                lesson.setStatus(LessonStatus.PENDING_TRAINER_CONFIRMATION);
                Lesson newLesson=calendarEventService.changeLessonTime(timeRequest.getStart(), timeRequest.getEnd(), lesson);
                if(lesson.getTrainer().getMessenger()== Messenger.TELEGRAM) {
                    telegramTrainerBot.send(lesson.getTrainer().getChatId(), "Lesson changed: " + newLesson.getStartTime().format(Constant.formatterDayTime) + " with student " + lesson.getStudent().getName());
                }
            } else {
                throw new RuntimeException("You are not allowed to change this lesson in status " + lesson.getStatus());
            }
        }
    }


    @GetMapping("/user/get_trainers")
    @PreAuthorize("hasRole('USER')")
    public List<UserShortDTO> findTrainers(HttpSession session) {
        User student = findUserFromSession(session);
        List<User> trainers = trainerStudentService.getAllTrainersByStudent(student.getId());
        return trainers.stream().map(UserShortDTO::toShortDTO).toList();
    }



}
