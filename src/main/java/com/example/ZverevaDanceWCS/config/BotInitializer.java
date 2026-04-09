package com.example.ZverevaDanceWCS.config;

import com.example.ZverevaDanceWCS.service.telegramBot.TelegramBot;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;


@Component
public class BotInitializer {
    private final TelegramBot telegramBot;

    public BotInitializer(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    @PostConstruct
    public void init() {
        try {
            TelegramBotsApi api =
                    new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(telegramBot);
    } catch  (TelegramApiException e){
            throw new RuntimeException("Ошибка запуска бота", e);
        }
    }
}

