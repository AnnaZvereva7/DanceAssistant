package com.example.ZverevaDanceWCS.service.controller;

import com.example.ZverevaDanceWCS.service.Constant;
import com.example.ZverevaDanceWCS.service.model.lessons.Lesson;
import com.example.ZverevaDanceWCS.service.model.lessons.lessonDTO.LessonFullDTO;
import com.example.ZverevaDanceWCS.service.model.lessons.LessonService;
import com.example.ZverevaDanceWCS.service.model.lessons.lessonDTO.LessonNewDTO;
import com.example.ZverevaDanceWCS.service.model.lessons.lessonDTO.LessonShortDTO;
import com.example.ZverevaDanceWCS.service.model.lessons.LessonStatus;
import com.example.ZverevaDanceWCS.service.model.lessons.lessonDTO.LessonUpdateDto;
import com.example.ZverevaDanceWCS.service.model.user.User;
import com.example.ZverevaDanceWCS.service.model.user.UserService;
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

    public LessonController(LessonService lessonService, UserService userService) {
        this.lessonService = lessonService;
        this.userService = userService;
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
        return LessonFullDTO.toFullDTO(lessonService.lessonCompleted(lessonService.findById(id)));
    }

    @PostMapping("lesson/new")
    public LessonFullDTO createNewLesson(@Valid @RequestBody LessonNewDTO lessonNewDTO) {
        log.info("Creating new lesson for student id="+lessonNewDTO.getStudentId()+" at "+lessonNewDTO.getStartTime());
        User student = userService.findById(lessonNewDTO.getStudentId());
        Lesson newLesson = lessonNewDTO.newLessonFromJson(student);
        Lesson savedLesson = lessonService.saveNewLesson(newLesson);
        log.info("New lesson created with id="+savedLesson.getId());
        return LessonFullDTO.toFullDTO(savedLesson);
    }

    @PutMapping("lesson/canceled/{id}")
    public LessonFullDTO markLessonAsCanceled(@PathVariable int id) {
        return LessonFullDTO.toFullDTO(lessonService.cancelLesson(lessonService.findById(id)));
    }

    @PutMapping("lesson/change")
    public LessonFullDTO changeLesson(@Valid @RequestBody LessonUpdateDto lessonUpdateDto) {
        Lesson lessonToUpdate = lessonService.findById(lessonUpdateDto.getLessonId());
        lessonToUpdate.setStartTime(lessonUpdateDto.getStartTime());
        lessonToUpdate.setDurationMin(lessonUpdateDto.getDurationInMinutes());
        lessonToUpdate.setCost(lessonUpdateDto.getCost());
        Lesson updatedLesson = lessonService.updateLesson(lessonToUpdate);
        return LessonFullDTO.toFullDTO(updatedLesson);
    }
}
