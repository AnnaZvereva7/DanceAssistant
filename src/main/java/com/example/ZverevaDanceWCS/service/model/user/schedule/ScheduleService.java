package com.example.ZverevaDanceWCS.service.model.user.schedule;

import com.example.ZverevaDanceWCS.service.Constant;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class ScheduleService {
    private final ScheduleRepository repository;

    public ScheduleService(ScheduleRepository repository) {
        this.repository = repository;
    }

    public Schedule saveSchedule(Schedule schedule) {
        return repository.save(schedule);
    }

    public void deleteById(int id) {
        repository.deleteById(id);
    }

    public void deleteSchedule(Schedule schedule) {
        repository.delete(schedule);
    }

    public List<Schedule> findByStudent(int studentId) {
       return repository.findByStudentId(studentId);

    }

    public Schedule findById(int id) {
        Optional<Schedule> schedule = repository.findById(id);
        return schedule.orElse(null);
    }

    public List<Schedule> findAllByStudentIdAndTrainerId (int studentId, int trainerId) {
        return repository.findByStudentIdAndTrainerId(studentId, trainerId);
    }

    public List<Schedule> findByTrainerId(int trainerId) {
        return repository.findByTrainerId(trainerId);
    }

    public Schedule saveNew(int trainerId, int studentId, String dateTime) {
        Schedule newSchedule = new Schedule();
        newSchedule.setTrainerId(trainerId);
        newSchedule.setStudentId(studentId);
        String[] dateTimeParts = dateTime.split(" ");
        newSchedule.setScheduleDay(DayOfWeek.valueOf(dateTimeParts[0]));
        newSchedule.setScheduleTime(LocalTime.parse(dateTimeParts[1], Constant.formatterTime));
        return repository.save(newSchedule);
    }
}
