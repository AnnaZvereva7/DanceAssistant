package com.example.ZverevaDanceWCS.service.model.trainerStudentLink;

import com.example.ZverevaDanceWCS.service.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainerStudentRepository extends JpaRepository<TrainerStudentLink, Long> {
    List<TrainerStudentLink> findAllByTrainerId(int trainerId);
    List<TrainerStudentLink> findAllByStudentId(int studentId);
    Optional<TrainerStudentLink> findByTrainerIdAndStudentId(int trainerId, int studentId);
}
