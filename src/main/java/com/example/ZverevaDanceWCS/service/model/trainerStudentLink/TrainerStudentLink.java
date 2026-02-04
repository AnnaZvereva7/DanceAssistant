package com.example.ZverevaDanceWCS.service.model.trainerStudentLink;

import com.example.ZverevaDanceWCS.service.model.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "trainer_student")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrainerStudentLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    int id;

    @ManyToOne
    @JoinColumn(name = "student_id", referencedColumnName = "user_id")
    User student;

    @ManyToOne
    @JoinColumn(name = "trainer_id", referencedColumnName = "user_id")
    User trainer;

    public User fromLinkGetStudent() {
        return this.student;
    }

    public User fromLinkGetTrainer() {
        return this.trainer;
    }

}
