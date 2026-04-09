package com.example.ZverevaDanceWCS.service.model.user.studentInfo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InfoRepository extends JpaRepository<StudentInfo, Long> {
    Optional<StudentInfo> findById(int infoId);
    List<StudentInfo> findByStudentId (Long studentId);
    List<StudentInfo> findByStudentIdAndStatus (Long studentId, InfoStatus status);
    List<StudentInfo> findByStudentIdAndTrainerIdAndStatus(Long studentId, Long trainerId, InfoStatus status);
    List<StudentInfo> findByStatusAndTrainerId(InfoStatus status, long trainerId);
}
