package com.example.ZverevaDanceWCS.service.controller;

import com.example.ZverevaDanceWCS.service.Constant;
import com.example.ZverevaDanceWCS.service.model.calendarEvent.CalendarEventService;
import com.example.ZverevaDanceWCS.service.model.lessons.Lesson;
import com.example.ZverevaDanceWCS.service.model.lessons.LessonService;
import com.example.ZverevaDanceWCS.service.model.lessons.LessonStatus;
import com.example.ZverevaDanceWCS.service.model.lessons.lessonDTO.LessonFullDTO;
import com.example.ZverevaDanceWCS.service.model.lessons.lessonDTO.LessonNewDTO;
import com.example.ZverevaDanceWCS.service.model.lessons.lessonDTO.LessonShortDTO;
import com.example.ZverevaDanceWCS.service.model.lessons.lessonDTO.LessonUpdateDto;
import com.example.ZverevaDanceWCS.service.model.payments.PaymentDTO;
import com.example.ZverevaDanceWCS.service.model.payments.PaymentService;
import com.example.ZverevaDanceWCS.service.model.user.schedule.Schedule;
import com.example.ZverevaDanceWCS.service.model.user.schedule.ScheduleService;
import com.example.ZverevaDanceWCS.service.model.user.schedule.ScheduleShortDTO;
import com.example.ZverevaDanceWCS.service.model.user.studentInfo.InfoService;
import com.example.ZverevaDanceWCS.service.model.user.studentInfo.NewInfoDTO;
import com.example.ZverevaDanceWCS.service.model.user.studentInfo.StudentInfo;
import com.example.ZverevaDanceWCS.service.model.user.*;
import com.example.ZverevaDanceWCS.service.model.user.userDTO.TrainerInfoDTO;
import com.example.ZverevaDanceWCS.service.model.user.userDTO.UserFullDTO;
import com.example.ZverevaDanceWCS.service.model.user.userDTO.UserShortDTO;
import com.example.ZverevaDanceWCS.service.model.user.userDTO.UserUpdateByAdminDto;
import com.example.ZverevaDanceWCS.service.telegramBot.TelegramStudentBot;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/trainer")
@PreAuthorize("hasRole('TRAINER') or hasRole('ADMIN')")
public class TrainerController {
    final UserService userService;
    final LessonService lessonService;
    final TelegramStudentBot bot;
    final PaymentService paymentService;
    final InfoService infoService;
    final ScheduleService scheduleService;
    final CalendarEventService calendarEventService;


    public TrainerController(UserService userService, UserRepository userRepository, LessonService lessonService, TelegramStudentBot bot, PaymentService paymentService, InfoService infoService, ScheduleService scheduleService, CalendarEventService calendarEventService) {
        this.userService = userService;
        this.lessonService = lessonService;
        this.bot = bot;
        this.paymentService = paymentService;
        this.infoService = infoService;
        this.scheduleService = scheduleService;
        this.calendarEventService = calendarEventService;
    }

    // Admin endpoints, user details by id
    @GetMapping("/user/{id}")
    public UserFullDTO getUserById(@PathVariable int id, HttpSession session) { //todo check if it's trainer's student
        User trainer = findTrainerFromSession(session);
        List<Schedule> schedules = scheduleService.findAllByStudentIdAndTrainerId(id, trainer.getId());
        return UserFullDTO.toFullDTO(userService.findByIdWithInfo(id), schedules);
    }

    @GetMapping("/user/{userId}/schedules")
    public List<ScheduleShortDTO> getStudentSchedules(@PathVariable int userId, HttpSession session) {
        User trainer = findTrainerFromSession(session);
        List<Schedule> schedules = scheduleService.findAllByStudentIdAndTrainerId(userId, trainer.getId());
        return schedules.stream().map(Schedule::toShortDto).toList();
    }


    //Admin endpoint, send bill to user by id in telegram
    @GetMapping("/user/send_bill/{id}")
    public Boolean sendBill(@PathVariable int id, HttpSession session) {//todo check if it's trainer's student
        User trainer = findTrainerFromSession(session);
        try {
            List<Lesson> lessons = lessonService.findByStatusAndStudentIdAndTrainerId(LessonStatus.COMPLETED, id, trainer.getId())
                    .stream()
                    .sorted()
                    .collect(Collectors.toList());

            String response = lessonService.lessonsToBill(lessons);
            User student = userService.findById(id);
            if (student.getMessenger() == Messenger.TELEGRAM) {
                bot.send(student.getChatId(), response);
            }
            bot.send(Constant.adminChatId, "Bill sent to " + student.getName() + ":\n" + response); //todo не отправлять в телеграмм ученика, но сделать чат в программе и там отправлять
            return true;
        } catch (RuntimeException e) {
            bot.send(Constant.adminChatId, "Failed to send bill to user id " + id + ": " + e.getMessage());
            return false;
        }

    }

    private User findTrainerFromSession(HttpSession session) {
        int trainerId = (int) session.getAttribute("USER_ID");
        return userService.findById(trainerId);
    }

    // Admin endpoint, get all users short info for the users list
    @GetMapping("/users")
    public List<UserShortDTO> getUsers(HttpSession session) {
        User trainer = findTrainerFromSession(session);
        log.info("trainer id=" + trainer.getId() + "trainer name " + trainer.getName() + " requested students list");
        List<User> students = userService.findAllByRoleAndTrainerId(null, trainer.getId());
        log.info("Found " + students.size() + " students for trainer id=" + trainer.getId());
        return students.stream().map(UserShortDTO::toShortDTO).toList();
    }

    // Admin endpoint, get all users balances for list from lowest to highest(0)
    @GetMapping("/users/balance")
    public List<PaymentDTO> getBalances(HttpSession session) {
        User trainer = findTrainerFromSession(session);
        return lessonService.findAllBalanceByTrainer(trainer.getId())
                .stream()
                .sorted(Comparator.comparing(PaymentDTO::getBalance))
                .toList();
    }

    // Admin endpoint, get user payment information (payment details) by id
    @GetMapping("/user/balance/{id}")
    public PaymentDTO getBalances(@PathVariable int id, HttpSession session) {
        User trainer = findTrainerFromSession(session);
        return lessonService.findBalanceByStudentIdAndTrainerId(id, trainer.getId());
    }

    // Admin endpoint, register payment from user by id and sum
    @PostMapping("/payment/{id}/{sum}")
    @Transactional
    public PaymentDTO paymentReceived(@PathVariable int id, @PathVariable int sum, HttpSession session) {
        User trainer = findTrainerFromSession(session);
        User student = userService.findById(id);
        log.info("trainer " + trainer.getName() + " registering payment of " + sum + " from student " + student.getName());
        paymentService.saveNew(student, trainer.getId(), sum, java.time.LocalDate.now());
        int balance = student.getBalance();
        balance += sum;
        student.setBalance(balance);
        userService.saveUser(student);
        lessonService.paymentToLessons(id, sum, trainer.getId());
        return lessonService.findBalanceByStudentIdAndTrainerId(id, trainer.getId());
    }

    // Admin endpoint, add new student info from DTO
    @PostMapping("/user/new_info")
    public StudentInfo addNewInfo(@Valid @RequestBody NewInfoDTO newInfo, HttpSession session) {
        User trainer = findTrainerFromSession(session);
        log.info("Adding new info for student id=" + newInfo.getStudentId());
        return infoService.saveFromNewInfoDTO(newInfo, trainer.getId());
    }

    // User endpoint, update user info from DTO
    @PutMapping("/user/change")
    public UserFullDTO updateUser(@Valid @RequestBody UserUpdateByAdminDto userUpdateByAdminDto, HttpSession session) {
        User userToUpdate = userService.findById(userUpdateByAdminDto.getStudentId());
        User trainer = findTrainerFromSession(session);
        if (!userToUpdate.getName().equals(userUpdateByAdminDto.getName())) {
            userToUpdate.setName(userUpdateByAdminDto.getName());
        }
        if (!userToUpdate.getRole().equals(userUpdateByAdminDto.getRole())) {
            userToUpdate.setRole(userUpdateByAdminDto.getRole());
        }
        if (!userUpdateByAdminDto.getEmail().isBlank()) {
            userToUpdate.setEmail(userUpdateByAdminDto.getEmail());
        } else if (userToUpdate.getEmail() != null) {
            userToUpdate.setEmail(null);
        }
        userService.saveUser(userToUpdate);

        List<Schedule> oldSchedules = scheduleService.findAllByStudentIdAndTrainerId(userToUpdate.getId(), trainer.getId());

        User savedUser = userService.findByIdWithInfo(userToUpdate.getId());
        return UserFullDTO.toFullDTO(savedUser, oldSchedules);
    }

    @DeleteMapping("user/schedule_delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSchedule(@PathVariable int id, HttpSession session) {
        User trainer = findTrainerFromSession(session);
        Schedule scheduleToDelete = scheduleService.findById(id);
        if (scheduleToDelete.getTrainerId() != trainer.getId()) {
            throw new RuntimeException("Schedule id=" + id + " does not belong to trainer " + trainer.getName());
        }
        scheduleService.deleteSchedule(scheduleToDelete);
    }

    @PostMapping("user/schedule_add/{currentStudentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addSchedule(@PathVariable int currentStudentId, @Validated @RequestBody ScheduleShortDTO newSchedule, HttpSession session) {
        log.info("dateTime received: " + newSchedule.getDateTime());
        User trainer = findTrainerFromSession(session);
        User student = userService.findById(currentStudentId);
        try {
            scheduleService.saveNew(trainer.getId(), student.getId(), newSchedule.getDateTime());
        } catch (RuntimeException e) {
            log.error("Failed to add new schedule for student id=" + currentStudentId + " at " + newSchedule.getDateTime() + " by trainer " + trainer.getName() + ": " + e.getMessage());
            throw new RuntimeException("Failed to add new schedule: " + e.getMessage());
        }
    }

    @GetMapping("/lesson/{id}")
    public LessonFullDTO getLessonById(@PathVariable Long id) {
        return LessonFullDTO.toFullDTO(lessonService.findById(id));
    }

    // Admin endpoint, all completed lessons
    @GetMapping("/lessons/completed")
    public List<LessonShortDTO> getLessonsCompleted(HttpSession session) {
        User trainer = findTrainerFromSession(session);
        return lessonService.findByStatusInAndTrainerId(List.of(LessonStatus.COMPLETED), trainer.getId())
                .stream().map(LessonShortDTO::toShortDTO).toList();
    }

    // Admin endpoint, all planned lessons
    @GetMapping("/lessons/planned")
    public List<LessonShortDTO> getLessonsPlanned(HttpSession session) {
        User trainer = findTrainerFromSession(session);
        return lessonService.findByStatusInAndTrainerId(List.of(LessonStatus.PLANNED, LessonStatus.PENDING_STUDENT_CONFIRMATION, LessonStatus.PENDING_TRAINER_CONFIRMATION), trainer.getId())
                .stream().map(LessonShortDTO::toShortDTO).toList();
    }

    // Admin endpoint, mark lesson as completed
    @PutMapping("/lesson/completed/{id}")
    public LessonFullDTO markLessonAsCompleted(@PathVariable Long id) {
        return LessonFullDTO.toFullDTO(lessonService.lessonCompleted(lessonService.findById(id)));
    }

    // Admin endpoint, create new lesson
    @PostMapping("/lesson/new")
    public LessonFullDTO createNewLesson(@Valid @RequestBody LessonNewDTO lessonNewDTO, HttpSession session) {
        User trainer = findTrainerFromSession(session);
        log.info("Creating new lesson for student id=" + lessonNewDTO.getStudentId() + " at " + lessonNewDTO.getStartTime());
        User student = userService.findById(lessonNewDTO.getStudentId());
        Lesson newLesson = lessonNewDTO.newLessonFromJson(student, trainer);
        Lesson savedLesson = lessonService.saveNewLesson(newLesson);
        log.info("New lesson created with id=" + savedLesson.getId());
        return LessonFullDTO.toFullDTO(savedLesson);
    }

    // Admin endpoint, mark lesson as canceled
    @PutMapping("/lesson/canceled/{id}")
    public LessonFullDTO markLessonAsCanceled(@PathVariable Long id) {
        return LessonFullDTO.toFullDTO(lessonService.cancelLesson(lessonService.findById(id)));
    }

    // Admin endpoint, change lesson details
    @PutMapping("/lesson/change")
    public LessonFullDTO changeLesson(@Valid @RequestBody LessonUpdateDto lessonUpdateDto) {
        Lesson lessonToUpdate = lessonService.findById(lessonUpdateDto.getLessonId());
        LocalDateTime newEndTime = lessonUpdateDto.getStartTime().plusMinutes(lessonUpdateDto.getDurationInMinutes());
        lessonToUpdate.setCost(lessonUpdateDto.getCost());
        Lesson updatedLesson =calendarEventService.changeLessonTime(lessonUpdateDto.getStartTime(), newEndTime, lessonToUpdate);
        return LessonFullDTO.toFullDTO(updatedLesson);
    }

    @GetMapping("/my_info")
    public TrainerInfoDTO getTrainerInfo(HttpSession session) {
        User trainer = findTrainerFromSession(session);
        return userService.getTrainerInfo(trainer);
    }

    @GetMapping("/refresh_link")
    public TrainerInfoDTO refreshCalendarLink(HttpSession session) {
        User trainer = findTrainerFromSession(session);
        return userService.refreshCalendarLink(trainer);
    }

}
