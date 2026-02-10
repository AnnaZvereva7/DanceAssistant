package com.example.ZverevaDanceWCS.service.model.freeSlots;

import com.example.ZverevaDanceWCS.service.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class FreeSlotService {
    final FreeSlotRepository repository;

    public FreeSlotService(FreeSlotRepository repository) {
        this.repository = repository;
    }

    public FreeSlot save(FreeSlot slot) {
        return repository.save(slot);
    }

    public void delete(Long slotId) {
        repository.deleteById(slotId);
    }

    public boolean checkIfSlotFree(LocalDateTime start, LocalDateTime end, int trainerId) {
        Optional<FreeSlot> slotOpt = repository.findByStartTimeBeforeAndEndTimeAfterAndTrainerId(start.plusMinutes(1), end.minusMinutes(1), trainerId);
        if(slotOpt.isPresent()) {log.info("Slot found: " + slotOpt.get().getStartTime().format(Constant.formatterDayTime));
        }else {
            log.info("slot not found");
        }
        if(slotOpt.isPresent()) {
            return true;
        } else {
            return false;
        }
    }

    public void addFreeTimeSlot(LocalDateTime start, LocalDateTime end, int trainerId) {
        Optional<FreeSlot> slotBeforeOpt=repository.findByEndTimeAndTrainerId(start, trainerId);
        Optional<FreeSlot> slotAfterOpt=repository.findByStartTimeAndTrainerId(end, trainerId);
       if(slotAfterOpt.isPresent()&&slotBeforeOpt.isPresent()) {
           FreeSlot slotBefore = slotBeforeOpt.get();
           FreeSlot slotAfter = slotAfterOpt.get();
           slotBefore.setEndTime(slotAfter.getEndTime());
           repository.delete(slotAfter);
           repository.save(slotBefore);
       } else if(slotBeforeOpt.isPresent()) {
              FreeSlot slotBefore = slotBeforeOpt.get();
              slotBefore.setEndTime(end);
              repository.save(slotBefore);
         } else if(slotAfterOpt.isPresent()) {
              FreeSlot slotAfter = slotAfterOpt.get();
              slotAfter.setStartTime(start);
              repository.save(slotAfter);
       } else  {
           FreeSlot slot = new FreeSlot();
           slot.setStartTime(start);
           slot.setEndTime(end);
           slot.setTrainerId(trainerId);
           repository.save(slot);
       }
    }

    public void bookPartOfFreeSlot(LocalDateTime start, LocalDateTime end, int trainerId) {
        Optional<FreeSlot> slotOpt = repository.findByStartTimeBeforeAndEndTimeAfterAndTrainerId(start.plusMinutes(1), start.minusMinutes(1), trainerId);
        if(slotOpt.isPresent()) {
            FreeSlot slot = slotOpt.get();
            if (slot.getStartTime().isEqual(start)) {
                if(slot.getEndTime().isBefore(end)||slot.getEndTime().isEqual(end)) {
                    repository.delete(slot);
                } else {
                    slot.setStartTime(end);
                    repository.save(slot);
                }
            } else {
                if (slot.getEndTime().isBefore(end)||slot.getEndTime().isEqual(end)) {
                    slot.setEndTime(start);
                    repository.save(slot);
                } else {
                    LocalDateTime newSLotEndTime = slot.getEndTime();
                    slot.setEndTime(start);
                    repository.save(slot);
                    FreeSlot newSlot = new FreeSlot();
                    newSlot.setStartTime(end);
                    newSlot.setEndTime(newSLotEndTime);
                    newSlot.setTrainerId(trainerId);
                    repository.save(newSlot);
                }
            }
        }
        slotOpt = repository.findByStartTimeBeforeAndEndTimeAfterAndTrainerId(end, end, trainerId);
        if(slotOpt.isPresent()) {
            FreeSlot slot = slotOpt.get();
            slot.setStartTime(end);
            repository.save(slot);
        }
    }

    public List<FreeSlot> findFreeSlotByTrainerId(int trainerId) {
        return repository.findByTrainerId(trainerId);
    }
}
