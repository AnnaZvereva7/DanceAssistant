package com.example.ZverevaDanceWCS.service.model.lessons;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LessonsRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByStudentId(int id);

    Optional<Lesson> findById(Long id);

    List<Lesson> findByStartTimeBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Lesson> findByStudentIdAndStartTimeBetween(int studentId, LocalDateTime startDate, LocalDateTime endDate);

    boolean existsByStudentIdAndStartTimeBetween(int studentId, LocalDateTime startDate, LocalDateTime endDate);

    List<Lesson> findByStatus(LessonStatus status);
    List<Lesson> findByStatusAndStudentId(LessonStatus status, int id);
    List<Lesson> findByStatusAndTrainerId(LessonStatus status, int id);
    List<Lesson> findByStatusAndStudentIdAndTrainerId(LessonStatus status, int studentId, int trainerId);
    List<Lesson> findByStatusInAndStartTimeBefore(List<LessonStatus> status, LocalDateTime date);

    List<Lesson> findByStudentIdAndStartTimeAfter(int studentId, LocalDateTime date);
    List<Lesson> findByStudentIdAndStatus(int studentId, LessonStatus status);
    List<Lesson> findByStatusInAndStartTimeAfter(List<LessonStatus> statusList, LocalDateTime timeNow);
    List<Lesson> findByStartTimeBetweenAndTrainerIdAndStatusNot(LocalDateTime startDateTime,LocalDateTime endDateTime, int trainerId, LessonStatus status);

    List<Lesson> findByStatusIn(List<LessonStatus> statuses);
    List<Lesson> findByStatusInAndTrainerId(List<LessonStatus> statuses, int trainerId);
    List<Lesson> findByStatusInAndStudentId(List<LessonStatus> statuses, int trainerId);
}
