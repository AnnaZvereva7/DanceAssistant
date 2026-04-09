package com.example.ZverevaDanceWCS.service.telegramBot.TelegramEnterCode;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class TelegramEnterCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "code_id")
    Long codeId;

    @Column(name = "user_id", nullable = false)
    Long userId;

    @Column(unique = true, nullable = false)
    String code;

    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;
    @Column(name = "expires_at", nullable = false)
    LocalDateTime expiresAt;

    public TelegramEnterCode(Long userId) {
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = this.createdAt.plusDays(1);
        this.code = generateCode(10);
    }

    public static String generateCode(int length) {
        SecureRandom RANDOM = new SecureRandom();
        String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }

        return sb.toString();
    }

}
