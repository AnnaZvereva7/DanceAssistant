package com.example.ZverevaDanceWCS.service.telegramBot;

import com.example.ZverevaDanceWCS.service.Constant;
import com.example.ZverevaDanceWCS.service.model.exception.NotFoundException;
import com.example.ZverevaDanceWCS.service.model.user.*;
import com.example.ZverevaDanceWCS.service.telegramBot.TelegramEnterCode.TelegramEnterCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
@PropertySource("application.properties")
public class TelegramBot extends TelegramLongPollingBot {

    @Value("${bot.name}")
    String botName;

    @Value("${bot.token}")
    String token;

    final UserService userService;
    final TelegramService telegramService;
    final TelegramEnterCodeService telegramEnterCodeService;

    @Autowired
    public TelegramBot(UserService userService,
                       TelegramService telegramtelegramService, TelegramEnterCodeService telegramEnterCodeService) {
        this.userService = userService;
        this.telegramService = telegramtelegramService;
        this.telegramEnterCodeService = telegramEnterCodeService;
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
                    onUpdateReceivedStudent(command, addInfo, chatId, name);
            } catch (NotFoundException e){
                log.info("New user with chatId: " + chatId + " name: " + name + " userName: " + userName +"text: " + messageText);
                onUpdateReceivedNewUser(messageText, chatId, name, userName);
            }
        }
    }

    //todo сделать для каждого пользователя отдельный код, сохранить в отдельной таблице в базе, чтоб действовал 1 день
    private void onUpdateReceivedNewUser(String text, Long chatId, String name, String chatName) {
        if (text.startsWith("/start")) {
            String[] parts = text.split(" ");
            String enterCode=parts[1];

            if (telegramEnterCodeService.isLinkValid(enterCode)) {
                Long userId = telegramEnterCodeService.findUserIdByTelegramEnterCode(enterCode);
                User student = userService.findById(userId);
                send(Constant.adminChatId, "New user started bot: " + name + " - " + chatName); //todo убрать это информирование в будущем
                student.setMessenger(Messenger.TELEGRAM);
                student.setChatId(chatId);
                student.setChatName(chatName);
                userService.saveUser(student);
                send(chatId, telegramService.startCommandRecieveUser(name));
            }
        }
    }

    private void onUpdateReceivedStudent(String command, String[] addInfo, long chatId, String name) {
        switch (command) {
            case "/start":
                send(chatId, telegramService.startCommandRecieveUser(name));
                break;
            case "/schedule":
                send(chatId, telegramService.schedule(chatId));
                break;
            case "/menu":
                send(chatId, telegramService.printMenuUser());
                break;
            case "/show_info":
                send(chatId, telegramService.showInfo(chatId));
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
