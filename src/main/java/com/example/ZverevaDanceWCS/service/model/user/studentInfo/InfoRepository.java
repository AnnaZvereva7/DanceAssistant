package com.example.ZverevaDanceWCS.service.model.user.studentInfo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InfoRepository extends JpaRepository<StudentInfo, Long> {
    Optional<StudentInfo> findById(int infoId);
    List<StudentInfo> findByStudentId (int studentId);
    List<StudentInfo> findByStudentIdAndStatus (int studentId, InfoStatus status);
    List<StudentInfo> findByStudentIdAndTrainerIdAndStatus(int studentId, int trainerId, InfoStatus status);
    List<StudentInfo> findByStatusAndTrainerId(InfoStatus status, int trainerId);
}
