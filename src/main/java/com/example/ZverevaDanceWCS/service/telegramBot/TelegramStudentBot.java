package com.example.ZverevaDanceWCS.service.telegramBot;

import com.example.ZverevaDanceWCS.service.Constant;
import com.example.ZverevaDanceWCS.service.model.exception.NotFoundException;
import com.example.ZverevaDanceWCS.service.model.user.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@PropertySource("application.properties")
public class TelegramStudentBot extends TelegramLongPollingBot {

    @Value("${bot.name}")
    String botName;

    @Value("${bot.token}")
    String token;

    @Value("${bot.start.code}")
    String startCode;

    final UserService userService;
    final TelegramStudentService telegramStudentService;

    @Autowired
    public TelegramStudentBot(UserService userService,
                              TelegramStudentService telegramtelegramStudentService) {
        this.userService = userService;
        this.telegramStudentService = telegramtelegramStudentService;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            String name = update.getMessage().getChat().getFirstName();
            String userName = update.getMessage().getChat().getUserName();
            messageText = messageText.trim();

            try{
                User student = userService.findByChatId(chatId);
                String command = getCommand(messageText);
                String[] addInfo = additionalCommandInformation(messageText, command, " ");
                if (student.getRole() == UserRole.NEW
                        || student.getRole() == UserRole.PERMANENT
                        || student.getRole() == UserRole.OLD) {
                    onUpdateReceivedStudent(command, addInfo, chatId, name);
                } else {
                    send(chatId, "With role " + student.getRole() + " you need write to other chat, text administrator"); //todo message for wrong role
                }
            } catch (NotFoundException e){
                log.info("New user with chatId: " + chatId + " name: " + name + " userName: " + userName +"text: " + messageText);
                onUpdateReceivedNewUser(messageText, chatId, name, userName);
            }
        }
    }

    private void onUpdateReceivedNewUser(String text, Long chatId, String name, String chatName) {
        if (text.startsWith("/start")) {
            String[] parts = text.split(" ");
            if (parts[1].equals(startCode)) {
                User student = userService.newUserTelegram(chatId, name, chatName);
                send(Constant.adminChatId, "New user started bot: " + name + " - " + chatName); //todo убрать это информирование в будущем
                log.info("New user started bot: " + name + " - " + chatName + " id=" + student.getId());
                send(chatId, telegramStudentService.startCommandRecieveUser(chatId, name));
            }
        }
    }

    private void onUpdateReceivedStudent(String command, String[] addInfo, long chatId, String name) {
        switch (command) {
            case "/start":
                send(chatId, telegramStudentService.startCommandRecieveUser(chatId, name));
                break;
            case "/schedule":
                send(chatId, telegramStudentService.scheduleForStudent(chatId));
                break;
            case "/menu":
                send(chatId, telegramStudentService.printMenuUser());
                break;
            case "show_info":
                send(chatId, telegramStudentService.showInfo(chatId));
            case "/new_lesson": //new_lesson:01.01.25 09:30 //todo как тут указать учителя? может старт код чтоб содержал id учителя?
                try {
                    HashMap<Long, String> responses = telegramStudentService.addLessonByStudent(addInfo, chatId);
                    for (Map.Entry<Long, String> entry : responses.entrySet()) {
                        send(entry.getKey(), entry.getValue());
                    }
                } catch (RuntimeException e) {
                    send(chatId, e.getMessage());
                }
                break;
            case "/change_lesson": //change_lesson:01.01.25 to 02.01.01 09:30
                try {
                    HashMap<Long, String> responses = telegramStudentService.changeLessonByStudent(addInfo, chatId);
                    for (Map.Entry<Long, String> entry : responses.entrySet()) {
                        send(entry.getKey(), entry.getValue());
                    }
                } catch (RuntimeException e) {
                    send(chatId, e.getMessage());
                }
                break;
            case "/cancel_lesson": //cancel_lesson:[01.01.25]
                try {
                    HashMap<Long, String> responses = telegramStudentService.cancelLessonByStudent(addInfo, chatId);
                    for (Map.Entry<Long, String> entry : responses.entrySet()) {
                        send(entry.getKey(), entry.getValue());
                    }
                } catch (RuntimeException e) {
                    send(chatId, e.getMessage());
                }
                break;
            default:
                send(chatId, Constant.CNR);
        }
    }

    private String[] additionalCommandInformation(String messageText, String command, String separator) {
        if (!command.equals(messageText)) {
            String text = messageText.substring(command.length() + 1);
            return text.split(separator);
        } else {
            return null;
        }

    }

    private String getCommand(String messageText) {
        if (messageText.contains(":")) {
            int index = messageText.indexOf(":");
            return messageText.substring(0, index).toLowerCase();
        } else {
            return messageText.toLowerCase();
        }
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    public void send(Long chatId, String text) {
        try {
            execute(new SendMessage(chatId.toString(), text));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
