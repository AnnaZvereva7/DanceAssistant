package com.example.ZverevaDanceWCS.service.model.user;

import com.example.ZverevaDanceWCS.service.Constant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

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

    @Transient
    String additionalInfo;

    @Column
    LocalDate birthday;

    @Enumerated(EnumType.STRING)
    Messenger messenger;

    @Column
    int balance;

    @Enumerated (EnumType.STRING)
    Language language;


    @Column(name="user_site_status")
    @Enumerated(EnumType.STRING)
    UserSiteStatus userSiteStatus;

    @Transient
    String schedule;

    @Transient
    List<User> trainers;



//    String googleAccessToken;
//    String googleRefreshToken;
//    LocalDateTime token_expiry;


    public User() {}

    public User(String name, String chatName, Messenger messenger, UserRole userRole) {
        this.name=name;
        this.chatName =chatName;
        this.role=userRole;
        this.messenger=messenger;
        this.balance=0;
        this.userSiteStatus=UserSiteStatus.ACTIVE;
    }

    public static Comparator<User> compareById() {
        return Comparator.comparing(User::getId);
    }

    public Boolean isTrainer() {
        return (this.getRole().equals(UserRole.TRAINER)||this.getRole().equals(UserRole.ADMIN));
    }
}
