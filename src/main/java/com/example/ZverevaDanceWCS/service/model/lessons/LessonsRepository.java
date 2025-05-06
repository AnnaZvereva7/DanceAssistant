package com.example.ZverevaDanceWCS.service.model.lessons;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LessonsRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByStudentId(int id);
    Lesson findById(int id);

    List<Lesson> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Lesson> findByStudentIdAndDateBetween(int studentId, LocalDateTime startDate, LocalDateTime endDate);

    boolean existsByStudentIdAndDateBetween(int studentId, LocalDateTime startDate, LocalDateTime endDate);

    List<Lesson> findByStatus(LessonStatus status);
    List<Lesson> findByStatusAndStudentName(LessonStatus status, String name);
    List<Lesson> findByStatusInAndDateBefore (List<LessonStatus> status, LocalDateTime date);
    List<Lesson> findByStudentIdAndDateAfter(int studentId, LocalDateTime date);
}
