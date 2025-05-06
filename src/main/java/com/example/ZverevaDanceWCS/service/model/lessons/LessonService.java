package com.example.ZverevaDanceWCS.service.model.lessons;

import com.example.ZverevaDanceWCS.service.model.exception.ExceptionForAdmin;
import com.example.ZverevaDanceWCS.service.model.exception.LessonNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LessonService {

    @Autowired
    private final LessonsRepository lessonsRepository;

    public LessonService(LessonsRepository lessonsRepository) {
        this.lessonsRepository = lessonsRepository;
    }

    public List<Lesson> findByStatus(LessonStatus status) {
        return lessonsRepository.findByStatus(status);
    }

    public Lesson findById (int id) {
        return lessonsRepository.findById(id);
    }

    public List<Lesson> findByStatusAndStudentName(LessonStatus status, String name) {
        return lessonsRepository.findByStatusAndStudentName(status, name);
    }

    public Lesson saveLesson(Lesson lesson) {
        return lessonsRepository.save(lesson);
    }

    public List<Lesson> findByDate(LocalDate date) {
        LocalDateTime startDate = date.atStartOfDay();
        LocalDateTime endDate = startDate.plusDays(1);
        return lessonsRepository.findByDateBetween(startDate, endDate);
    }

    public List<Lesson> findInPeriod(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atStartOfDay().plusDays(1);
        return lessonsRepository.findByDateBetween(startDateTime, endDateTime);
    }

    public List<Lesson> findByStudentAndPeriod(int studentId, LocalDate startDate, LocalDate endDate) {
        return lessonsRepository.findByStudentIdAndDateBetween(studentId, startDate.atStartOfDay(), endDate.atStartOfDay().plusDays(1));
    }

    public Lesson findByStudentDate(LocalDate date, int studentId) {
        LocalDateTime startDate = date.atStartOfDay();
        LocalDateTime endDate = startDate.plusDays(1);
        List<Lesson> lessons = lessonsRepository.findByStudentIdAndDateBetween(studentId, startDate, endDate);
        if(lessons.isEmpty()) {
            throw new LessonNotFoundException("No lessons on this date");
        } else if (lessons.size()==1) {
            return lessons.get(0);
        } else {
            throw new ExceptionForAdmin("More then one lesson on one date");
        }
    }

    public boolean existByStudentDate (LocalDate date, int studentId) {
        LocalDateTime startDate = date.atStartOfDay();
        LocalDateTime endDate = startDate.plusDays(1);
        return lessonsRepository.existsByStudentIdAndDateBetween(studentId, startDate, endDate);
    }

    public List<Lesson> findPassedNotCompletedLessons() {
        return lessonsRepository.findByStatusInAndDateBefore(List.of(LessonStatus.NEW, LessonStatus.PLANNED), LocalDateTime.now());
    }

    public List<Lesson> findByStudentAndDateAfter(int studentId, LocalDateTime dateTime) {
        return lessonsRepository.findByStudentIdAndDateAfter(studentId, dateTime);
    }

}
