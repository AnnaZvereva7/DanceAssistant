package com.example.ZverevaDanceWCS.service.controller;

import com.example.ZverevaDanceWCS.service.Constant;
import com.example.ZverevaDanceWCS.service.model.lessons.Lesson;
import com.example.ZverevaDanceWCS.service.model.lessons.lessonDTO.LessonFullDTO;
import com.example.ZverevaDanceWCS.service.model.lessons.LessonService;
import com.example.ZverevaDanceWCS.service.model.lessons.lessonDTO.LessonNewDTO;
import com.example.ZverevaDanceWCS.service.model.lessons.lessonDTO.LessonShortDTO;
import com.example.ZverevaDanceWCS.service.model.lessons.LessonStatus;
import com.example.ZverevaDanceWCS.service.model.lessons.lessonDTO.LessonUpdateDto;
import com.example.ZverevaDanceWCS.service.model.user.Messenger;
import com.example.ZverevaDanceWCS.service.model.user.User;
import com.example.ZverevaDanceWCS.service.model.user.UserService;
import com.example.ZverevaDanceWCS.service.telegramBot.TelegramStudentBot;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
public class LessonController {
    final LessonService lessonService;
    final UserService userService;
    final TelegramStudentBot studentBot;

    public LessonController(LessonService lessonService, UserService userService, TelegramStudentBot studentBot) {
        this.lessonService = lessonService;
        this.userService = userService;
        this.studentBot = studentBot;
    }

    @GetMapping("/lesson/{id}")
    public LessonFullDTO getLessonById(@PathVariable int id) {
        return LessonFullDTO.toFullDTO(lessonService.findById(id));
    }

    @GetMapping("/lessons/completed")
    public List<LessonShortDTO> getLessonsCompleted() {
        return lessonService.findByStatusIn(List.of(LessonStatus.COMPLETED))
        .stream().map(LessonShortDTO::toShortDTO).toList();
    }

    @GetMapping("/lessons/planned")
    public List<LessonShortDTO> getLessonsPlanned() {
        return lessonService.findByStatusIn(List.of(LessonStatus.PLANNED, LessonStatus.NEW))
                .stream().map(LessonShortDTO::toShortDTO).toList();
    }

    @PutMapping("lesson/completed/{id}")
    public LessonFullDTO markLessonAsCompleted(@PathVariable int id) {
        Lesson lesson = lessonService.lessonCompleted(lessonService.findById(id));
        if(lesson.getStudent().getMessenger()== Messenger.TELEGRAM) {
            studentBot.send(lesson.getStudent().getChatId(),
                    "Lesson on " + lesson.getStartTime().format(Constant.timeFormatter) + " marked as completed. Thank you!");
        }
        return LessonFullDTO.toFullDTO(lesson);
    }

    @PostMapping("lesson/new")
    public LessonFullDTO createNewLesson(@Valid @RequestBody LessonNewDTO lessonNewDTO) {
        log.info("Creating new lesson for student id="+lessonNewDTO.getStudentId()+" at "+lessonNewDTO.getStartTime());
        User student = userService.findById(lessonNewDTO.getStudentId());
        Lesson newLesson = lessonNewDTO.newLessonFromJson(student);
        Lesson savedLesson = lessonService.saveNewLesson(newLesson);
        //todo add telegram message to student if teacher created lesson
        log.info("New lesson created with id="+savedLesson.getId());
        if(student.getMessenger()==Messenger.TELEGRAM) {
            studentBot.send(student.getChatId(),
                    "New lesson scheduled on " + savedLesson.getStartTime().format(Constant.timeFormatter) +
                            " for " + savedLesson.getDurationMin() + " minutes. See you!");
        }
        return LessonFullDTO.toFullDTO(savedLesson);
    }

    @PutMapping("lesson/canceled/{id}")
    public LessonFullDTO markLessonAsCanceled(@PathVariable int id) {
        Lesson lesson=lessonService.cancelLesson(lessonService.findById(id));
        if(lesson.getStudent().getMessenger()==Messenger.TELEGRAM) {
            studentBot.send(lesson.getStudent().getChatId(),
                    "Lesson on " + lesson.getStartTime().format(Constant.timeFormatter) + " has been canceled.");
        }
        return LessonFullDTO.toFullDTO(lesson);
    }

    @PutMapping("lesson/change")
    public LessonFullDTO changeLesson(@Valid @RequestBody LessonUpdateDto lessonUpdateDto) {
        Lesson lessonToUpdate = lessonService.findById(lessonUpdateDto.getLessonId());
        Lesson updatedLesson = lessonToUpdate;
        updatedLesson.setStartTime(lessonUpdateDto.getStartTime());
        updatedLesson.setDurationMin(lessonUpdateDto.getDurationInMinutes());
        updatedLesson.setCost(lessonUpdateDto.getCost());
        updatedLesson = lessonService.updateLesson(updatedLesson);
        if(updatedLesson.getStudent().getMessenger()==Messenger.TELEGRAM) {
            studentBot.send(updatedLesson.getStudent().getChatId(),
                    "Lesson on " + lessonToUpdate.getStartTime().format(Constant.timeFormatter) +
                            " were changed. New time " +updatedLesson.getStartTime().format(Constant.timeFormatter)
                            +" for " + updatedLesson.getDurationMin() + " minutes.");
        }
        return LessonFullDTO.toFullDTO(updatedLesson);
    }
}
