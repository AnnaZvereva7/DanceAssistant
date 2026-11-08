package com.example.ZverevaDanceWCS.service.model.mailService;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "calendar_event_sequences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Sequence {
    @Id
    @Column(name = "lesson_id")
    private Long lessonId;

    @Column(name= "sequence_number", nullable = false)
    private Integer sequence = 0;
}
