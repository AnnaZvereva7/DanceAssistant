package com.example.ZverevaDanceWCS.config;

import com.example.ZverevaDanceWCS.service.telegramBot.TelegramStudentBot;
import com.example.ZverevaDanceWCS.service.telegramBot.TelegramTrainerBot;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.List;

@Component
public class BotInitializer {
    private final List<TelegramLongPollingBot> bots;

    public BotInitializer(List<TelegramLongPollingBot> bots) {
        this.bots = bots;
    }

    @PostConstruct
    public void init() throws TelegramApiException {
        TelegramBotsApi api =
                new TelegramBotsApi(DefaultBotSession.class);

        for (TelegramLongPollingBot bot : bots) {
            api.registerBot(bot);
        }
    }
}

