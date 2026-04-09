package com.example.ZverevaDanceWCS.service.model.lessons;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LessonsRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByStudentId(Long id);

    Optional<Lesson> findById(Long id);

    List<Lesson> findByStartTimeBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Lesson> findByStudentIdAndStartTimeBetween(Long studentId, LocalDateTime startDate, LocalDateTime endDate);

    boolean existsByStudentIdAndStartTimeBetween(Long studentId, LocalDateTime startDate, LocalDateTime endDate);

    List<Lesson> findByStatus(LessonStatus status);
    List<Lesson> findByStatusAndStudentId(LessonStatus status, Long id);
    List<Lesson> findByStatusAndTrainerId(LessonStatus status, Long id);
    List<Lesson> findByStatusAndStudentIdAndTrainerId(LessonStatus status, Long studentId, Long trainerId);
    List<Lesson> findByStatusInAndStartTimeBefore(List<LessonStatus> status, LocalDateTime date);

    List<Lesson> findByStudentIdAndStartTimeAfter(Long studentId, LocalDateTime date);
    List<Lesson> findByStudentIdAndStatus(Long studentId, LessonStatus status);
    List<Lesson> findByStatusInAndStartTimeAfter(List<LessonStatus> statusList, LocalDateTime timeNow);
    List<Lesson> findByStartTimeBetweenAndTrainerIdAndStatusNot(LocalDateTime startDateTime,LocalDateTime endDateTime, Long trainerId, LessonStatus status);

    List<Lesson> findByStatusIn(List<LessonStatus> statuses);
    List<Lesson> findByStatusInAndTrainerId(List<LessonStatus> statuses, Long trainerId);
    List<Lesson> findByStatusInAndStudentId(List<LessonStatus> statuses, Long trainerId);
}
