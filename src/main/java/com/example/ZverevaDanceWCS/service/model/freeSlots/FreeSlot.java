package com.example.ZverevaDanceWCS.service.model.freeSlots;

import com.example.ZverevaDanceWCS.service.model.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "free_slots")
public class FreeSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "slot_id")
    Long id;

    @ManyToOne
    @JoinColumn(name = "trainer_id", referencedColumnName = "user_id", nullable = false)
    User trainer;

    @Column(name = "start_date_time", nullable = false)
    LocalDateTime startTime;

    @Column(name = "end_date_time", nullable = false)
    LocalDateTime endTime;
}
