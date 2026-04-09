package com.example.ZverevaDanceWCS.service.telegramBot.TelegramEnterCode;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TelegramEnterCodeRepository extends JpaRepository<TelegramEnterCode, Long> {
    boolean existsByCode (String code);
    Optional<TelegramEnterCode> findByCode(String code);
}
