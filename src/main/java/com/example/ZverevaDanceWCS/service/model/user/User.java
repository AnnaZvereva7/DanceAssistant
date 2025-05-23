package com.example.ZverevaDanceWCS.service.model.user;

import com.example.ZverevaDanceWCS.service.Constant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    int id;

    @Column
    String name;

    @Column(name = "chat_name")
    String chatName;

    @Column (name = "chat_id",unique = true)
    Long chatId;

    @Column(unique = true)
    String email;

    @Enumerated(EnumType.STRING)
    UserRole role;

    @Column
    String plans;

    @Column
    LocalDate birthday;

    @Enumerated(EnumType.STRING)
    Messenger messenger;

    @Column
    int balance;

    @Enumerated (EnumType.STRING)
    Language language;

    @Enumerated (EnumType.STRING)
    @Column(name="schedule_day")
    DayOfWeek scheduleDay;

    @Column(name = "schedule_time")
    LocalTime scheduleTime;

//    String googleAccessToken;
//    String googleRefreshToken;
//    LocalDateTime token_expiry;


    public User() {}

    public User(String name, String chatName, String messenger, UserRole userRole) {
        this.name=name;
        this.chatName =chatName;
        this.role=userRole;
        this.messenger=Messenger.valueOf(messenger.toUpperCase());
        this.balance=0;
    }

    public String toStringSchedule () {
        return this.getName()+" ("+this.getId()+") -schedule: "+this.scheduleDay+ " "+this.scheduleTime.format(Constant.timeFormatter);
    }

}
