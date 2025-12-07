package com.example.ZverevaDanceWCS.service.controller;

import com.example.ZverevaDanceWCS.service.Constant;
import com.example.ZverevaDanceWCS.service.model.lessons.Lesson;
import com.example.ZverevaDanceWCS.service.model.lessons.LessonService;
import com.example.ZverevaDanceWCS.service.model.lessons.LessonStatus;
import com.example.ZverevaDanceWCS.service.model.lessons.lessonDTO.LessonFullDTO;
import com.example.ZverevaDanceWCS.service.model.lessons.lessonDTO.LessonNewDTO;
import com.example.ZverevaDanceWCS.service.model.lessons.lessonDTO.LessonShortDTO;
import com.example.ZverevaDanceWCS.service.model.lessons.lessonDTO.LessonUpdateDto;
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
import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController("/admin")
public class AdminController {
    final UserService userService;
    final LessonService lessonService;
    final TelegramBot bot;
    final PaymentService paymentService;
    final InfoService infoService;

    public AdminController(UserService userService, UserRepository userRepository, LessonService lessonService, TelegramBot bot, PaymentService paymentService, InfoService infoService) {
        this.userService = userService;
        this.lessonService = lessonService;
        this.bot = bot;
        this.paymentService = paymentService;
        this.infoService = infoService;
    }

    // Admin endpoints, user details by id
    @GetMapping("/user/{id}")
    public UserFullDTO getUserById(@PathVariable int id) {
        return UserFullDTO.toFullDTO(userService.findByIdWithInfo(id));
    }

    //Admin endpoint, send bill to user by id in telegram
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

    // Admin endpoint, get all users short info for the users list
    @GetMapping("/users")
    public List<UserShortDTO> getUsers() {
        return userService.findAllByRole(null).stream().map(UserShortDTO::toShortDTO).toList();
    }

    // Admin endpoint, get all users balances for list from lowest to highest(0)
    @GetMapping("/users/balance")
    public List<PaymentDTO> getBalances() {
        return lessonService.findAllBalance()
                .stream()
                .sorted(Comparator.comparing(PaymentDTO::getBalance))
                .toList();
    }

    // Admin endpoint, get user payment information (payment details) by id
    @GetMapping("/user/balance/{id}")
    public PaymentDTO getBalances(@PathVariable int id) {
        return lessonService.findBalanceByStudentId(id);
    }

    // Admin endpoint, register payment from user by id and sum
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

    // Admin endpoint, add new student info from DTO
    @PostMapping ("/user/new_info")
    public StudentInfo addNewInfo(@Valid @RequestBody NewInfoDTO newInfo) {
        log.info("Adding new info for student id="+newInfo.getStudentId());
        return infoService.saveFromNewInfoDTO(newInfo);
    }

    // User endpoint, update user info from DTO
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

    // Admin endpoint, all completed lessons
    @GetMapping("/lessons/completed")
    public List<LessonShortDTO> getLessonsCompleted() {
        return lessonService.findByStatusIn(List.of(LessonStatus.COMPLETED))
                .stream().map(LessonShortDTO::toShortDTO).toList();
    }

    // Admin endpoint, all planned lessons
    @GetMapping("/lessons/planned")
    public List<LessonShortDTO> getLessonsPlanned() {
        return lessonService.findByStatusIn(List.of(LessonStatus.PLANNED, LessonStatus.NEW))
                .stream().map(LessonShortDTO::toShortDTO).toList();
    }

    // Admin endpoint, mark lesson as completed
    @PutMapping("/lesson/completed/{id}")
    public LessonFullDTO markLessonAsCompleted(@PathVariable int id) {
        return LessonFullDTO.toFullDTO(lessonService.lessonCompleted(lessonService.findById(id)));
    }

    // Admin endpoint, create new lesson
    @PostMapping("/lesson/new")
    public LessonFullDTO createNewLesson(@Valid @RequestBody LessonNewDTO lessonNewDTO) {
        log.info("Creating new lesson for student id="+lessonNewDTO.getStudentId()+" at "+lessonNewDTO.getStartTime());
        User student = userService.findById(lessonNewDTO.getStudentId());
        Lesson newLesson = lessonNewDTO.newLessonFromJson(student);
        Lesson savedLesson = lessonService.saveNewLesson(newLesson);
        log.info("New lesson created with id="+savedLesson.getId());
        return LessonFullDTO.toFullDTO(savedLesson);
    }

    // Admin endpoint, mark lesson as canceled
    @PutMapping("/lesson/canceled/{id}")
    public LessonFullDTO markLessonAsCanceled(@PathVariable int id) {
        return LessonFullDTO.toFullDTO(lessonService.cancelLesson(lessonService.findById(id)));
    }

    // Admin endpoint, change lesson details
    @PutMapping("/lesson/change")
    public LessonFullDTO changeLesson(@Valid @RequestBody LessonUpdateDto lessonUpdateDto) {
        Lesson lessonToUpdate = lessonService.findById(lessonUpdateDto.getLessonId());
        lessonToUpdate.setStartTime(lessonUpdateDto.getStartTime());
        lessonToUpdate.setDurationMin(lessonUpdateDto.getDurationInMinutes());
        lessonToUpdate.setCost(lessonUpdateDto.getCost());
        Lesson updatedLesson = lessonService.updateLesson(lessonToUpdate);
        return LessonFullDTO.toFullDTO(updatedLesson);
    }

}
