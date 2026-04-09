package com.example.ZverevaDanceWCS.service.model.trainerStudentLink;

import com.example.ZverevaDanceWCS.service.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainerStudentRepository extends JpaRepository<TrainerStudentLink, Long> {
    List<TrainerStudentLink> findAllByTrainerId(Long trainerId);
    List<TrainerStudentLink> findAllByStudentId(Long studentId);
    Optional<TrainerStudentLink> findByTrainerIdAndStudentId(Long trainerId, Long studentId);
}
