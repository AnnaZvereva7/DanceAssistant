package com.example.ZverevaDanceWCS.service.telegramBot;

import com.example.ZverevaDanceWCS.service.model.exception.CommandNotRecognizedException;
import com.example.ZverevaDanceWCS.service.model.exception.NotFoundException;
import com.example.ZverevaDanceWCS.service.model.user.Messenger;
import com.example.ZverevaDanceWCS.service.model.user.User;
import com.example.ZverevaDanceWCS.service.model.user.UserRole;
import com.example.ZverevaDanceWCS.service.model.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.Objects;

@Component
@Slf4j
@PropertySource("application.properties")
public class TelegramTrainerBot extends TelegramLongPollingBot {
    final TelegramStudentBot studentBot;

    @Value("${bot.trainer.name}")
    private String botUsername;

    @Value("${bot.trainer.token}")
    private String botToken;

    @Value("${bot.trainer.start.code}")
    private String startCode;

    final UserService userService;
    final TelegramTrainerService serviceTrainer;

    public TelegramTrainerBot(TelegramStudentBot studentBot, UserService userService, TelegramTrainerService serviceTrainer) {
        this.studentBot = studentBot;
        this.userService = userService;
        this.serviceTrainer = serviceTrainer;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            String name = update.getMessage().getChat().getFirstName();
            String chatName = update.getMessage().getChat().getUserName();
            messageText = messageText.trim();

            try {
                User user = userService.findByChatId(chatId);
                if (user.getRole() == UserRole.TRAINER || userService.findByChatId(chatId).getRole() == UserRole.ADMIN) {
                    String command = getCommand(messageText);
                    String[] addInfo = additionalCommandInformation(messageText, command, "-");
                    onUpdateReceivedTrainer(command, addInfo, chatId, name);
                }
            } catch(NotFoundException e){
                    onUpdateReceivedNewTrainer(messageText, chatId, name, chatName);
                }
            }
        }

        private void onUpdateReceivedNewTrainer (String text, Long chatId, String name, String
        chatName){
            if (text.startsWith("/start")) {
                String[] parts = text.split(" ");
                if (parts[1].equals(startCode)) {
                    try {
                        User user = userService.findByChatId(chatId);
                        user.setRole(UserRole.TRAINER);
                        userService.saveUser(user);
                        send(chatId, "Hi, " + name + ")!");
                    } catch (NotFoundException e) {
                        User newUser = new User(name, chatName, Messenger.TELEGRAM, UserRole.TRAINER);
                        newUser.setChatId(chatId);
                        userService.saveUser(newUser);
                        log.info("New user started trainer_bot: " + name + " - " + chatName + " id=" + newUser.getId());
                        send(chatId, "Hi, " + name + ")!");
                    }
                }
            }
        }

        private void onUpdateReceivedTrainer (String command, String[]addInfo, Long chatId, String name){
            HashMap<Long, String> responses = new HashMap<>();
            switch (command) {
                case "/start":
                    send(chatId, "Hi, " + name + ")!");
                    break;
                case "/menu":
                    send(chatId, serviceTrainer.printMenuAdmin());
                    break;
                case "/schedule": //расписание на 7 дней от текущей даты(начало дня)
                    send(chatId, serviceTrainer.schedule());
                    break;
                case "/planned_lessons":
                    send(chatId, serviceTrainer.plannedLessons());
                    break;
                case "/scheduled_students":
                    send(chatId, serviceTrainer.scheduledStudents());
                    break;
                case "/add_by_schedule":
                    responses = serviceTrainer.addBySchedule(chatId);
                    processingHashMapResponse(responses, chatId);
                    break;
                case "/student_list":
                    try {
                        send(chatId, serviceTrainer.studentList(addInfo));
                    } catch (CommandNotRecognizedException e) {
                        send(chatId, "Error in command execution, check the data format");
                    }
                    ;
                    break;
                case "/month_payments":
                    try {
                        send(chatId, serviceTrainer.monthPayment(addInfo));
                    } catch (CommandNotRecognizedException e) {
                        send(chatId, "Error in command execution, check the data format");
                    }
                    break;
                case "/year_payments":
                    try {
                        send(chatId, serviceTrainer.yearPayments(addInfo));
                    } catch (CommandNotRecognizedException e) {
                        send(chatId, "Error in command execution, check the data format");
                    }
                    break;
                case "/new_lesson": //id 01.01.25 09:30 - добавить урок по id ученика, сразу статус  PLANNED
                    try {
                        responses = serviceTrainer.newLesson(chatId, addInfo);
                        processingHashMapResponse(responses, chatId);
                    } catch (CommandNotRecognizedException e) {
                        send(chatId, "Error in command execution, check the data format");
                    }
                    break;
                case "/new_student": //name chat_name messenger role (PERMANENT, NEW)
                    try {
                        send(chatId, serviceTrainer.newStudent(addInfo));
                    } catch (CommandNotRecognizedException e) {
                        send(chatId, "Error in command execution, check the data format");
                    }
                    break;
                case "/unpaid":
                    send(chatId, serviceTrainer.unpaidLessons(addInfo));
                    break;
                case "/lesson_completed": //01.01.25 [id] (если без id то все уроки за дату выполнены)
                    try {
                        responses = serviceTrainer.lessonCompleted(chatId, addInfo);
                        processingHashMapResponse(responses,chatId);
                    } catch (CommandNotRecognizedException e) {
                        send(chatId, "Error in command execution, check the data format");
                    }
                    break;
                case "/student_change": //student_change_status:id newStatus
                    try {
                        send(chatId, serviceTrainer.changeStudent(addInfo));
                    } catch (CommandNotRecognizedException e) {
                        send(chatId, "Error in command execution, check the data format");
                    }
                    break;
                case "/change_lesson": //change_lesson:id 01.01 to 02.01.25 09:30 - change lesson
                    try {
                        responses = serviceTrainer.changeLessonTime(chatId, addInfo);
                        processingHashMapResponse(responses,chatId);
                    } catch (CommandNotRecognizedException e) {
                        send(chatId, "Error in command execution, check the data format");
                    }
                    break;
                case "/balance": //balance:[id]
                    try {
                        send(chatId, serviceTrainer.showBalance(addInfo));
                    } catch (CommandNotRecognizedException e) {
                        send(chatId, "Error in command execution, check the data format");
                    }
                    break;
                case "/payment": //payment:id-sum
                    try {
                        responses = serviceTrainer.paymentReceived(chatId, addInfo);
                        processingHashMapResponse(responses,chatId);
                    } catch (CommandNotRecognizedException e) {
                        send(chatId, "Error in command execution, check the data format");
                    }
                    break;
                case "/change_schedule"://change_schedule:id-[dayOfWeek-time]
                    try {
                        responses = serviceTrainer.changeSchedule(chatId, addInfo);
                        processingHashMapResponse(responses, chatId);
                    } catch (CommandNotRecognizedException e) {
                        send(chatId, "Error in command execution, check the data format");
                    }
                    break;
                case "/delete_schedule"://delete_schedule:id
                    try {
                        send(chatId, serviceTrainer.deleteSchedule(addInfo));
                    } catch (CommandNotRecognizedException e) {
                        send(chatId, "Error in command execution, check the data format");
                    }
                    break;
                case "/add_info":
                    try {
                        responses = serviceTrainer.addNewInfo(chatId, addInfo);
                        processingHashMapResponse(responses,chatId);
                    } catch (CommandNotRecognizedException e) {
                        send(chatId, "Error in command execution, check the data format");
                    }
                    serviceTrainer.addNewInfo(chatId, addInfo);
                    break;
                case "/show_info": //"show_info:studentId
                    try {
                        send(chatId, serviceTrainer.showInfo(addInfo));
                    } catch (CommandNotRecognizedException e) {
                        send(chatId, "Error in command execution, check the data format");
                    }
                    break;
                case "/info_old":
                    try {
                        send(chatId, serviceTrainer.infoChangeStatus(addInfo));
                    } catch (CommandNotRecognizedException e) {
                        send(chatId, "Error in command execution, check the data format");
                    }
                    break;
                case "/cancel_lesson": //cancel_lesson:lessonId
                    try {
                        responses = serviceTrainer.cancelLesson(chatId, addInfo);
                        processingHashMapResponse(responses, chatId);
                    } catch (CommandNotRecognizedException e) {
                        send(chatId, "Error in command execution, check the data format");
                    }
                    break;
                case "/send_bill": //send_bill:studentId
                    try {
                        responses = serviceTrainer.sendBill(addInfo, chatId);
                        processingHashMapResponse(responses,chatId);
                    } catch (CommandNotRecognizedException e) {
                        send(chatId, "Error in command execution, check the data format");
                    }
                    break;
                case "/change_duration":
                    try {
                        send(chatId, serviceTrainer.changeDuration(addInfo));
                    } catch (CommandNotRecognizedException e) {
                        send(chatId, "Error in command execution, check the data format");
                    }
                    break;
                default:
                    send(chatId, "Command not recognized, please check the command format");
            }
        }

        private String[] additionalCommandInformation (String messageText, String command, String separator){
            if (!command.equals(messageText)) {
                String text = messageText.substring(command.length() + 1);
                return text.split(separator);
            } else {
                return null;
            }

        }

        private String getCommand (String messageText){
            if (messageText.contains(":")) {
                int index = messageText.indexOf(":");
                return messageText.substring(0, index).toLowerCase();
            } else {
                return messageText.toLowerCase();
            }
        }

        @Override
        public String getBotUsername () {
            return botUsername;
        }

        @Override
        public String getBotToken () {
            return botToken;
        }

        public void send (Long chatId, String text){
            try {
                execute(new SendMessage(chatId.toString(), text));
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }

        private void processingHashMapResponse(HashMap<Long, String> responses, Long chatId) {
            for (HashMap.Entry<Long, String> entry : responses.entrySet()) {
                if(Objects.equals(entry.getKey(), chatId)) {
                    send(entry.getKey(), entry.getValue());
                } else {
                    studentBot.send(entry.getKey(), entry.getValue());
                }
            }
        }

    }
