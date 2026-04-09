package com.example.ZverevaDanceWCS.service.telegramBot.TelegramEnterCode;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TelegramEnterCodeService {
    private final TelegramEnterCodeRepository telegramEnterCodeRepository;

    public TelegramEnterCodeService(TelegramEnterCodeRepository telegramEnterCodeRepository) {
        this.telegramEnterCodeRepository = telegramEnterCodeRepository;
    }

    public TelegramEnterCode save(TelegramEnterCode telegramEnterCode) {
        return telegramEnterCodeRepository.save(telegramEnterCode);
    }

    public Boolean isCodeExists(String newCode) {
        return telegramEnterCodeRepository.existsByCode(newCode);
    }

    public String createNewLink(Long userId) {
        String newLink="https://t.me/TestDanceAssistanceBot?start=";
        TelegramEnterCode newCode= new TelegramEnterCode(userId);
        while (isCodeExists(newCode.getCode())) {
            newCode = new TelegramEnterCode(userId);
        }
        save(newCode);
        newLink=newLink+newCode.getCode();
        return newLink;
    }

    public boolean isLinkValid(String codeString) {
        Optional<TelegramEnterCode> codeOpt = telegramEnterCodeRepository.findByCode(codeString);
        if(codeOpt.isPresent()){
            TelegramEnterCode code = codeOpt.get();
            if (code.getExpiresAt().isAfter(java.time.LocalDateTime.now())) {
                return true;
            } else {
                telegramEnterCodeRepository.delete(code);
                return false;
            }
        } else {
            return false;
        }
    }

     public Long findUserIdByTelegramEnterCode(String codeString) {
        TelegramEnterCode telegramEnterCode= telegramEnterCodeRepository.findByCode(codeString).orElseThrow(() -> new RuntimeException("Invalid code"));
        return telegramEnterCode.getUserId();
     }
}
