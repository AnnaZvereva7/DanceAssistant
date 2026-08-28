package com.example.ZverevaDanceWCS.service.model.mailCalendarEvent;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SequenceService {
    private final SequenceRepository repository;

    public SequenceService(SequenceRepository repository) {
        this.repository = repository;
    }

    public int getOrCreate (Long lessonId) {
        Optional<Sequence> sequenceOpt = repository.findByLessonId(lessonId);
        if(sequenceOpt.isPresent()) {
            return sequenceOpt.get().getSequence();
        } else {
            Sequence newSequence = new Sequence(lessonId, 0);
            repository.save(newSequence);
            return newSequence.getSequence();
        }
    }

    public int increment (Long lessonId) {
        Optional<Sequence> sequenceOpt = repository.findByLessonId(lessonId);
        if (sequenceOpt.isPresent()) {
            Sequence sequence = sequenceOpt.get();
            sequence.setSequence(sequence.getSequence()+1);
            repository.save(sequence);
            return sequence.getSequence();
        } else {
            Sequence sequence= new Sequence(lessonId, 0);
            repository.save(sequence);
            return sequence.getSequence();
        }
    }
}
