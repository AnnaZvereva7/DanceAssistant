package com.example.ZverevaDanceWCS.service.model.freeSlots;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FreeSlotRepository extends JpaRepository<FreeSlot, Long> {
    List<FreeSlot> findByTrainerId(Long trainerId);
    Optional<FreeSlot> findByStartTimeBeforeAndEndTimeAfterAndTrainerId(LocalDateTime startTime, LocalDateTime endTime, Long trainerId);
    Optional<FreeSlot> findByStartTimeAndTrainerId(LocalDateTime startTime, Long trainerId);
    Optional<FreeSlot> findByEndTimeAndTrainerId(LocalDateTime endTime, Long trainerId);

}
