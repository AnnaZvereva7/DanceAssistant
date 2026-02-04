package com.example.ZverevaDanceWCS.service.model.user.schedule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByStudentId(int studentId);
    List<Schedule> findByTrainerId(int trainerId);
    List<Schedule> findByStudentIdAndTrainerId(int studentId, int trainerId);
    Optional<Schedule> findById(int id);
    void deleteById(int id);
}
