package com.example.ZverevaDanceWCS.service.model.subscriptions;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "subscription_types")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_type_id")
    Long id;

    @Column (name = "trainer_id", nullable = false)
    Long trainerId;

    @Column (name = "trainer_name", nullable = false)
    String trainerName;

    @Column (name ="duration_months", nullable = false)
    int durationMonths;

    @Column(name = "amount_lessons", nullable = false)
    int amountLessons;

    @Column (name = "price", nullable = false)
    int price;

    @Column (name = "active", nullable = false)
    Boolean active;

    @Column (name = "comment", nullable = false)
    String comment;

    @Override
    public String toString() {
        return amountLessons+" lessons for "+this.price +" EUR (valid for "+this.durationMonths+" months)";
    }
}
