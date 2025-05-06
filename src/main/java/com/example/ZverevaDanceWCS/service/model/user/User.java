package com.example.ZverevaDanceWCS.service.model.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
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


    public User() {}

    public User(String name, String chatName, String messenger, UserRole userRole) {
        this.name=name;
        this.chatName =chatName;
        this.role=userRole;
        this.messenger=Messenger.valueOf(messenger.toUpperCase());
        this.balance=0;
    }

}
