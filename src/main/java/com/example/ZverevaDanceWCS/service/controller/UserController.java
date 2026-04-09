package com.example.ZverevaDanceWCS.service.controller;

import com.example.ZverevaDanceWCS.service.model.lessons.Lesson;
import com.example.ZverevaDanceWCS.service.model.lessons.LessonStatus;
import com.example.ZverevaDanceWCS.service.model.lessons.LessonService;
import com.example.ZverevaDanceWCS.service.model.lessons.lessonDTO.LessonShortDTO;
import com.example.ZverevaDanceWCS.service.model.lessons.lessonDTO.LessonUserDTO;
import com.example.ZverevaDanceWCS.service.model.payments.Payment;
import com.example.ZverevaDanceWCS.service.model.payments.PaymentDTO;
import com.example.ZverevaDanceWCS.service.model.payments.PaymentService;
import com.example.ZverevaDanceWCS.service.model.payments.TransactionDataDao;
import com.example.ZverevaDanceWCS.service.model.user.User;
import com.example.ZverevaDanceWCS.service.model.user.UserService;
import com.example.ZverevaDanceWCS.service.model.user.schedule.Schedule;
import com.example.ZverevaDanceWCS.service.model.user.schedule.ScheduleService;
import com.example.ZverevaDanceWCS.service.model.user.schedule.ScheduleShortDTO;
import com.example.ZverevaDanceWCS.service.model.user.userDTO.UserUpdateByUserDto;
import com.example.ZverevaDanceWCS.service.telegramBot.TelegramEnterCode.TelegramEnterCodeService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

import java.time.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@RestController
@RequestMapping("/user")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public class UserController {
    final LessonService lessonService;
    final UserService userService;
    final PaymentService paymentService;
    final ScheduleService scheduleService;
    final TelegramEnterCodeService telegramEnterCodeService;

    public UserController(LessonService lessonService, UserService userService, PaymentService paymentService, ScheduleService scheduleService, TelegramEnterCodeService telegramEnterCodeService) {
        this.lessonService = lessonService;
        this.userService = userService;
        this.paymentService = paymentService;
        this.scheduleService = scheduleService;
        this.telegramEnterCodeService = telegramEnterCodeService;
    }

    @PostMapping("/confirm_lesson/{lessonId}")
    public LessonUserDTO confirmLesson(@PathVariable Long lessonId, HttpSession session) {
        Long userId = (Long) session.getAttribute("USER_ID");
        Lesson lesson = lessonService.findById(lessonId);
        if (!lesson.getStudent().getId().equals(userId)) {
            throw new RuntimeException("User is not a student of this lesson");
        }
        return new LessonUserDTO(lessonService.confirmLesson(lessonId, LessonStatus.PENDING_STUDENT_CONFIRMATION));
    }

    @PostMapping("/cancel_lesson/{lessonId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelLesson(@PathVariable Long lessonId, HttpSession session) {
        Long userId = (Long) session.getAttribute("USER_ID");
        Lesson lesson = lessonService.findById(lessonId);
        if (!lesson.getStudent().getId().equals(userId)) {
            throw new RuntimeException("User is not a student of this lesson");
        }
        if (lesson.getStatus()!=LessonStatus.PENDING_STUDENT_CONFIRMATION&&lesson.getStatus()!=LessonStatus.PENDING_TRAINER_CONFIRMATION) {
            throw new RuntimeException("Lesson cannot be canceled at this stage");
        }
        lessonService.cancelLesson(lesson);
    }

    //User endpoint, update details
    @PutMapping("/change")
    public UserUpdateByUserDto updateUserDetails(@Valid @RequestBody UserUpdateByUserDto userDto, HttpSession session) {
        Long userId = (Long) session.getAttribute("USER_ID");
        return userService.updateByUser(userDto, userId);
    }

    //User and Admin endpoint
    @GetMapping("/schedule")
    public List<LessonUserDTO> getLessonByStudentId(HttpSession session) {
        Long userId = (Long) session.getAttribute("USER_ID");
        return lessonService.findByStudentAndDateAfter(userId, LocalDateTime.now())
                .stream()
                .filter(lesson -> lesson.getStatus() != LessonStatus.CANCELED)
                .sorted(Comparator.comparing(Lesson::getStartTime))
                .map(lesson -> new LessonUserDTO(lesson))
                .toList();
    }

    //User endpoint, get details
    @GetMapping("/my_info")
    public UserUpdateByUserDto getUserDetails(HttpSession session) {
        Long userId = (Long) session.getAttribute("USER_ID");
        List<ScheduleShortDTO> schedules = scheduleService.findByStudent(userId)
                .stream()
                .map(Schedule::toShortDto)
                .toList();
        return UserUpdateByUserDto.fromUser(userService.findByIdWithInfo(userId), schedules);
    }

    @GetMapping("/to_pay")
    public PaymentDTO toPay(HttpSession session) { //todo сделать список по разным преподавателям
        Long userId = (Long) session.getAttribute("USER_ID");
        return lessonService.findBalanceByStudentId(userId);
    }

    @GetMapping("/month_details/{month}/{year}")
    public List<TransactionDataDao> getPaymentsLessons(@PathVariable String month, @PathVariable int year, HttpSession session) {
        Long userId = (Long) session.getAttribute("USER_ID");
        Month monthEnum = Month.valueOf(month.toUpperCase());

        YearMonth yearMonth = YearMonth.of(year, monthEnum);

        LocalDate start = yearMonth
                .atDay(1);

        LocalDate end = yearMonth
                .atEndOfMonth();

        List<Lesson> lessons = lessonService.findByStudentAndPeriod(userId, start, end)
                .stream()
                .filter(lesson -> lesson.getStatus() != LessonStatus.CANCELED)
                .toList();
        List<Payment> payments = paymentService.findByStudentAndPeriod(userId, start, end);

        List<TransactionDataDao> result = new ArrayList<>();
        result = Stream.concat(
                        lessons.stream().map(lesson -> TransactionDataDao.fromLesson(lesson)),
                        payments.stream().map(payment -> TransactionDataDao.fromPayment(payment))
                ).sorted(Comparator.comparing(TransactionDataDao::getDate))
                .toList();
        return result;
    }

    @GetMapping("/telegram_link")
    public Map<String,String> getTelegramLink(HttpSession session) {
        Long userId = (Long) session.getAttribute("USER_ID");
        String response= telegramEnterCodeService.createNewLink(userId);
        log.info(response);
        return Map.of("link", response);
    }

}
