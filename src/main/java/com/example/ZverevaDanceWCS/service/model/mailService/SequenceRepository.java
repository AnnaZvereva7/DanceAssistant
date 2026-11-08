package com.example.ZverevaDanceWCS.service.model.mailService;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SequenceRepository extends JpaRepository <Sequence, Long> {
    Optional<Sequence> findByLessonId(Long lessonId);
}
