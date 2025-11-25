package com.example.ZverevaDanceWCS.service.controller;

import com.example.ZverevaDanceWCS.service.Constant;
import com.example.ZverevaDanceWCS.service.model.lessons.Lesson;
import com.example.ZverevaDanceWCS.service.model.lessons.LessonService;
import com.example.ZverevaDanceWCS.service.model.lessons.LessonStatus;
import com.example.ZverevaDanceWCS.service.model.payments.PaymentDTO;
import com.example.ZverevaDanceWCS.service.model.payments.PaymentService;
import com.example.ZverevaDanceWCS.service.model.studentInfo.InfoService;
import com.example.ZverevaDanceWCS.service.model.studentInfo.NewInfoDTO;
import com.example.ZverevaDanceWCS.service.model.studentInfo.StudentInfo;
import com.example.ZverevaDanceWCS.service.model.user.*;
import com.example.ZverevaDanceWCS.service.model.user.userDTO.UserFullDTO;
import com.example.ZverevaDanceWCS.service.model.user.userDTO.UserShortDTO;
import com.example.ZverevaDanceWCS.service.model.user.userDTO.UserUpdateDto;
import com.example.ZverevaDanceWCS.service.telegramBot.TelegramBot;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class UserController {
    final UserService userService;
    final LessonService lessonService;
    final TelegramBot bot;
    final PaymentService paymentService;
    final InfoService infoService;

    public UserController(UserService userService, UserRepository userRepository, LessonService lessonService, TelegramBot bot, PaymentService paymentService, InfoService infoService) {
        this.userService = userService;
        this.lessonService = lessonService;
        this.bot = bot;
        this.paymentService = paymentService;
        this.infoService = infoService;
    }

    @GetMapping("/user/{id}")
    public UserFullDTO getUserById(@PathVariable int id) {
        return UserFullDTO.toFullDTO(userService.findByIdWithInfo(id));
    }

    @GetMapping("/user/send_bill/{id}")
    public Boolean sendBill(@PathVariable int id) {
        try {
            List<Lesson> lessons = lessonService.findByStatusAndStudentId(LessonStatus.COMPLETED, id)
                    .stream()
                    .sorted()
                    .collect(Collectors.toList());

            String response = lessonService.lessonsToBill(lessons);
            User student = userService.findById(id);
            if (student.getMessenger() == Messenger.TELEGRAM) {
                bot.sendMessage(student.getChatId(), response);
            }
            bot.sendMessage(Constant.adminChatId, "Bill sent to " + student.getName() + ":\n" + response);
            return true;
        } catch (RuntimeException e) {
            bot.sendMessage(Constant.adminChatId, "Failed to send bill to user id " + id + ": " + e.getMessage());
            return false;
        }

    }

    @GetMapping("/users")
    public List<UserShortDTO> getUsers() {
        return userService.findAllByRole(null).stream().map(UserShortDTO::toShortDTO).toList();
    }

    @GetMapping("/users/balance")
    public List<PaymentDTO> getBalances() {
        return lessonService.findAllBalance()
                .stream()
                .sorted(Comparator.comparing(PaymentDTO::getBalance))
                .toList();
    }

    @GetMapping("/user/balance/{id}")
    public PaymentDTO getBalances(@PathVariable int id) {
        return lessonService.findBalanceByStudentId(id);
    }

    @PostMapping("/payment/{id}/{sum}")
    @Transactional
    public PaymentDTO paymentReceived(@PathVariable int id, @PathVariable int sum) {
        User student = userService.findById(id);
        paymentService.saveNew(student, sum, java.time.LocalDate.now());
        int balance = student.getBalance();
        balance += sum;
        student.setBalance(balance);
        userService.saveUser(student);
        lessonService.paymentToLessons(id, sum);
        return lessonService.findBalanceByStudentId(id);
    }

    @PostMapping ("/user/new_info")
    public StudentInfo addNewInfo(@Valid @RequestBody NewInfoDTO newInfo) {
        log.info("Adding new info for student id="+newInfo.getStudentId());
        return infoService.saveFromNewInfoDTO(newInfo);
    }

    @PutMapping("/user/change")
    public UserFullDTO updateUser(@Valid @RequestBody UserUpdateDto userUpdateDto) {
        User userToUpdate = userService.findById(userUpdateDto.getStudentId());
        userToUpdate.setName(userUpdateDto.getName());
        userToUpdate.setRole(userUpdateDto.getRole());
        if(userUpdateDto.getEmail()!=null) {
            userToUpdate.setEmail(userUpdateDto.getEmail());
        }
        if(userUpdateDto.getSchedule()!=null && userUpdateDto.getSchedule().equals("Not set")) {
            userToUpdate.setScheduleDay(null);
            userToUpdate.setScheduleTime(null);
        } else if (userUpdateDto.getSchedule()!=null) {
            String[] scheduleParts = userUpdateDto.getSchedule().split(" ");
            userToUpdate.setScheduleDay(DayOfWeek.valueOf(scheduleParts[0]));
            userToUpdate.setScheduleTime(java.time.LocalTime.parse(scheduleParts[1]));
        }
        User savedUser = userService.saveUser(userToUpdate);
        return UserFullDTO.toFullDTO(savedUser);
    }

}
