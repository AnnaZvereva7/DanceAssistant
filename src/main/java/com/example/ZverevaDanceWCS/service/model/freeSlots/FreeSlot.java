package com.example.ZverevaDanceWCS.service.model.freeSlots;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "slots")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FreeSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "slot_id")
    Long id;

    @Column(name = "trainer_id", nullable = false)
    int trainerId;


    @Column(name = "start_date_time", nullable = false)
    LocalDateTime startTime;

    @Column(name = "end_date_time", nullable = false)
    LocalDateTime endTime;

}
