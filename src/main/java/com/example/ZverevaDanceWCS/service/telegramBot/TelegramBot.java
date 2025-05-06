package com.example.ZverevaDanceWCS.service.telegramBot;

import com.example.ZverevaDanceWCS.config.BotConfig;
import com.example.ZverevaDanceWCS.service.Constant;
import com.example.ZverevaDanceWCS.service.model.exception.ExceptionForAdmin;
import com.example.ZverevaDanceWCS.service.model.user.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig config;
    @Autowired
    final UserService userService;
    @Autowired
    final private TelegramBotAdmin botAdmin;
    @Autowired
    final private TelegramBotUser botUser;

    public TelegramBot(BotConfig config, UserService userService, TelegramBotAdmin botAdmin, TelegramBotUser botUser) {
        this.config = config;
        this.userService = userService;
        this.botAdmin = botAdmin;
        this.botUser = botUser;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            String name = update.getMessage().getChat().getFirstName();
            String userName = update.getMessage().getChat().getUserName();
            if (userName.equals(Constant.adminTelegramName)) {
                onUpdateReceivedAdmin(messageText, chatId, name);
                //onUpdateReceivedUser(messageText, chatId, name, userName); //TODO убрать после тестирования user interface
            } else {
                onUpdateReceivedUser(messageText, chatId, name, userName);
            }
        }
    }

    private void onUpdateReceivedAdmin(String messageText, long chatId, String name) {
        messageText = messageText.trim();
        String command = getCommand(messageText);
        String[] addInfo = additionalCommandInformation(messageText, command, "-");
        String answer;
        switch (command) {
            case "/start":
                answer = "Hi, " + name + " (" + chatId + ")!";
                sendMessage(chatId, answer);
                break;
            case "/menu":
                sendMessage(chatId, botAdmin.printMenuAdmin());
                break;
            case "/schedule": //расписание на 7 дней от текущей даты(начало дня)
                sendMessage(chatId, botAdmin.schedule());
                break;
            case "/student_list":
                sendMessage(chatId, botAdmin.studentList(addInfo));
                break;
            case "/new_lesson": //id 01.01.25 09:30 - добавить урок по id ученика, сразу статус  PLANNED, уведомление только админу
                sendMessage(chatId, botAdmin.newLesson(addInfo));
                break;
            case "/new_student": //name chat_name messenger role (PERMANENT, NEW)
                sendMessage(chatId, botAdmin.newStudent(addInfo));
                break;
            case "/lesson_list": //startDate [endDate] список занятий за период(один день) LessonShortDao
                sendMessage(chatId, botAdmin.lessonList(addInfo));
                break;
            case "/unpaid":
                sendMessage(chatId, botAdmin.unpaidLessons(addInfo));
                break;
            case "/lesson_completed": //01.01.25 [id] (если без id то все уроки за дату выполнены)
                HashMap<Long, String> answers = botAdmin.lessonCompleted(addInfo);
                for (Map.Entry<Long, String> entry : answers.entrySet()) {
                    sendMessage(entry.getKey(), entry.getValue());
                }
                break;
            case "/student_change": //student_change_status:id newStatus
                try{
                    sendMessage(Constant.adminChatId, botAdmin.changeStudent(addInfo));
                } catch (RuntimeException e) {
                    sendMessage(Constant.adminChatId, e.getMessage());
                }
                break;
            case"/change_lesson": //change_lesson:id 01.01 to 02.01.25 09:30 - change lesson
                try {
                    HashMap<Long, String> response = botAdmin.changeLessonTime(addInfo);
                    for(Map.Entry<Long, String> entry : response.entrySet()) {
                        sendMessage(entry.getKey(), entry.getValue());
                    }
                } catch (RuntimeException e) {
                    sendMessage(Constant.adminChatId, e.getMessage());
                }
                break;
            case "/show_plans": //show_plans:[id]
                try{
                    sendMessage(Constant.adminChatId, botAdmin.showPlans(addInfo));
                } catch (RuntimeException e) {
                    sendMessage(Constant.adminChatId, e.getMessage());
                }
                break;
            case "/balance": //balance:[id]
                try{
                    sendMessage(Constant.adminChatId, botAdmin.showBalance(addInfo));
                } catch (RuntimeException e) {
                    sendMessage(Constant.adminChatId, e.getMessage());
                }
                break;
            case "/add_recap": //add_recap:lessonId-recap
                try{
                    HashMap<Long, String > response = botAdmin.recap(addInfo);
                    for(Map.Entry<Long, String> entry: response.entrySet()){
                        sendMessage(entry.getKey(), entry.getValue());
                    }
                } catch (RuntimeException e) {
                    sendMessage(Constant.adminChatId, e.getMessage());
                }
                break;
                default:
                sendMessage(chatId, "Command not recognized");
        }
    }

    private void onUpdateReceivedUser(String messageText, long chatId, String name, String userName) {
        messageText = messageText.trim();
        String command = getCommand(messageText);
        String[] addInfo = additionalCommandInformation(messageText, command, " ");
        String answer;
        switch (command) {
            case "/start":
                HashMap<Long, String> response = botUser.startCommandRecieveUser(chatId, name, userName);
                for(Map.Entry<Long, String> entry: response.entrySet()) {
                    sendMessage(entry.getKey(), entry.getValue());
                }
                break;
            case "/schedule":
               sendMessage(chatId, botUser.schedule(chatId));
                break;
            case "/menu":
               sendMessage(chatId, botUser.printMenuUser());
                break;
            case "/new_lesson": //new_lesson:01.01.25 09:30\
                try {
                    answer = botUser.addLesson(addInfo, chatId);
                    sendMessage(chatId, answer);
                    sendMessage(Constant.adminChatId, answer + " - " + userName);//todo hashMap
                } catch (RuntimeException e) {
                    sendMessage(chatId, e.getMessage());
                }
                break;
            case "/change_lesson": //change_lesson:01.01.25 to 02.01.01 09:30
                try {
                    answer = botUser.changeLesson(addInfo, chatId);
                    sendMessage(chatId, answer);
                    sendMessage(Constant.adminChatId, answer + " - "+userName);//todo hashMap
                } catch (RuntimeException e) {
                    sendMessage(chatId, e.getMessage());
                }
                break;
            case "/cancel_lesson": //cancel_lesson:[01.01.25]
                try {
                    answer=botUser.cancelLesson(addInfo, chatId);
                    sendMessage(chatId, answer);
                    sendMessage(Constant.adminChatId, answer+" - "+userName);//todo hashMap
                } catch (RuntimeException e) {
                    sendMessage(chatId, e.getMessage());
                }
                break;
            case "lesson_in_period": //lessons_in_period:01.01.25 [31.05.25]
                try {
                    answer=botUser.lessonsInPeriod(addInfo, chatId);
                    sendMessage(chatId, answer);
                } catch (ExceptionForAdmin e) {
                    sendMessage(Constant.adminChatId, e.getMessage());
                } catch (RuntimeException e) {
                    sendMessage(chatId, e.getMessage());
                }
                break;
            default:
                sendMessage(chatId, Constant.CNR);
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

    public void sendMessage(long chatId, String answer) {
        SendMessage message = new SendMessage(String.valueOf(chatId), answer);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            //log
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }
}
