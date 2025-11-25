package com.example.ZverevaDanceWCS.service.model.studentInfo;

import com.example.ZverevaDanceWCS.service.model.payments.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InfoRepository extends JpaRepository<StudentInfo, Long> {
    Optional<StudentInfo> findById(int infoId);
    List<StudentInfo> findByStudentId (int studentId);
    List<StudentInfo> findByStudentIdAndStatus (int studentId, InfoStatus status);
    List<StudentInfo> findByStatus (InfoStatus status);
}
