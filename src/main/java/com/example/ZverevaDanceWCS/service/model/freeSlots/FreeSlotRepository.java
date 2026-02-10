package com.example.ZverevaDanceWCS.service.model.freeSlots;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FreeSlotRepository extends JpaRepository<FreeSlot, Long> {
    List<FreeSlot> findByTrainerId(int trainerId);
    Optional<FreeSlot> findByStartTimeBeforeAndEndTimeAfterAndTrainerId(LocalDateTime startTime, LocalDateTime endTime, int trainerId);
    Optional<FreeSlot> findByStartTimeAndTrainerId(LocalDateTime startTime, int trainerId);
    Optional<FreeSlot> findByEndTimeAndTrainerId(LocalDateTime endTime, int trainerId);
    Optional<FreeSlot> findByStartTimeBeforeAndTrainerId(LocalDateTime startTime, int trainerId);
    Optional<FreeSlot> findByStartTimeBetweenAndEndTimeBetweenAndTrainerId(LocalDateTime strat1, LocalDateTime end1,
                                                                           LocalDateTime start2, LocalDateTime end2, int trainerId);
}
