package com.example.ZverevaDanceWCS.service.model.lessons;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LessonsRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByStudentId(int id);
    Lesson findById(int id);

    List<Lesson> findByStartTimeBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Lesson> findByStudentIdAndStartTimeBetween(int studentId, LocalDateTime startDate, LocalDateTime endDate);

    boolean existsByStudentIdAndStartTimeBetween(int studentId, LocalDateTime startDate, LocalDateTime endDate);

    List<Lesson> findByStatus(LessonStatus status);
    List<Lesson> findByStatusAndStudentId(LessonStatus status, int id);
    List<Lesson> findByStatusInAndStartTimeBefore(List<LessonStatus> status, LocalDateTime date);
    List<Lesson> findByStudentIdAndStartTimeAfter(int studentId, LocalDateTime date);
    List<Lesson> findByStudentIdAndStatus(int studentId, LessonStatus status);
    List<Lesson> findByStatusInAndStartTimeAfter(List<LessonStatus> statusList, LocalDateTime timeNow);
    List<Lesson> findByStartTimeBetweenAndStatusNot(LocalDateTime startDateTime,LocalDateTime endDateTime, LessonStatus status);
    List<Lesson> findByRecapIsNotNull();
    List<Lesson> findByStatusIn(List<LessonStatus> statuses);
}
