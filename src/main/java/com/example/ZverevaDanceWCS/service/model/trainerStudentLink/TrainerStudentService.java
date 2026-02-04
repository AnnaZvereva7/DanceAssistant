package com.example.ZverevaDanceWCS.service.model.trainerStudentLink;


import com.example.ZverevaDanceWCS.service.model.user.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrainerStudentService {
    final TrainerStudentRepository repository;

    public TrainerStudentService(TrainerStudentRepository repository) {
        this.repository = repository;
    }

    public void saveLink(User trainer, User student) {
        if( !repository.findByTrainerIdAndStudentId(trainer.getId(), student.getId()).isPresent()) {
            TrainerStudentLink link = new TrainerStudentLink();
            link.setTrainer(trainer);
            link.setStudent(student);
            repository.save(link);
        }
    }

    public List<User> getAllStudentsByTrainer(int trainerId) {
        List<TrainerStudentLink> links = repository.findAllByTrainerId(trainerId);
        return links.stream().map(TrainerStudentLink::fromLinkGetStudent).toList();
    }

    public List<User> getAllTrainersByStudent(int studentId) {
        return repository.findAllByStudentId(studentId).stream().map(TrainerStudentLink::fromLinkGetTrainer).toList();
    }
}
