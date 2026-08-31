package com.example.ZverevaDanceWCS.service.controller;

import com.example.ZverevaDanceWCS.service.model.appCalendarEvent.CalendarEventService;
import com.example.ZverevaDanceWCS.service.model.lessons.Lesson;
import com.example.ZverevaDanceWCS.service.model.lessons.LessonService;
import com.example.ZverevaDanceWCS.service.model.lessons.LessonStatus;
import com.example.ZverevaDanceWCS.service.model.lessons.lessonDTO.LessonFullDTO;
import com.example.ZverevaDanceWCS.service.model.lessons.lessonDTO.LessonNewDTO;
import com.example.ZverevaDanceWCS.service.model.lessons.lessonDTO.LessonShortDTO;
import com.example.ZverevaDanceWCS.service.model.lessons.lessonDTO.LessonUpdateDto;
import com.example.ZverevaDanceWCS.service.model.payments.PaymentDTO;
import com.example.ZverevaDanceWCS.service.model.payments.PaymentService;
import com.example.ZverevaDanceWCS.service.model.subscriptions.StudentSubscription;
import com.example.ZverevaDanceWCS.service.model.subscriptions.SubscriptionService;
import com.example.ZverevaDanceWCS.service.model.subscriptions.dto.*;
import com.example.ZverevaDanceWCS.service.model.subscriptions.SubscriptionTypeService;
import com.example.ZverevaDanceWCS.service.model.trainerStudentLink.TrainerStudentService;
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
import com.example.ZverevaDanceWCS.service.model.user.userDTO.UserUpdateByTrainerDto;
import com.example.ZverevaDanceWCS.service.telegramBot.TelegramBot;
import com.example.ZverevaDanceWCS.service.telegramBot.TelegramEnterCode.TelegramEnterCodeService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.service.annotation.PutExchange;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/trainer")
@PreAuthorize("hasRole('TRAINER') or hasRole('ADMIN')")
public class TrainerController {
    final UserService userService;
    final LessonService lessonService;
    final TelegramBot bot;
    final PaymentService paymentService;
    final InfoService infoService;
    final ScheduleService scheduleService;
    final CalendarEventService calendarEventService;
    final TrainerStudentService trainerStudentService;
    final TelegramEnterCodeService telegramEnterCodeService;
    final SubscriptionTypeService subscriptionTypeService;
    final SubscriptionService subscriptionService;


    public TrainerController(UserService userService, LessonService lessonService, TelegramBot bot, PaymentService paymentService, InfoService infoService, ScheduleService scheduleService, CalendarEventService calendarEventService, TrainerStudentService trainerStudentService, TelegramEnterCodeService telegramEnterCodeService, SubscriptionTypeService subscriptionTypeService, SubscriptionService subscriptionService) {
        this.userService = userService;
        this.lessonService = lessonService;
        this.bot = bot;
        this.paymentService = paymentService;
        this.infoService = infoService;
        this.scheduleService = scheduleService;
        this.calendarEventService = calendarEventService;
        this.trainerStudentService = trainerStudentService;
        this.telegramEnterCodeService = telegramEnterCodeService;
        this.subscriptionTypeService = subscriptionTypeService;
        this.subscriptionService = subscriptionService;
    }

    @PostMapping("new_user/{name}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addNewUser(@PathVariable String name, HttpSession session) {
        if (name.isBlank()) {
            throw new RuntimeException("Name cannot be blank");
        }
        User trainer = findTrainerFromSession(session);
        User newUser = userService.newUser(name);
        trainerStudentService.saveLink(trainer, newUser);
    }


    // Admin endpoints, user details by id
    @GetMapping("/user/{id}")
    public UserFullDTO getUserById(@PathVariable Long id, HttpSession session) { //todo check if it's trainer's student
        User trainer = findTrainerFromSession(session);
        List<Schedule> schedules = scheduleService.findAllByStudentIdAndTrainerId(id, trainer.getId());
        return UserFullDTO.toFullDTO(userService.findByIdWithInfo(id), schedules);
    }

    @GetMapping("/user/{userId}/schedules")
    public List<ScheduleShortDTO> getStudentSchedules(@PathVariable Long userId, HttpSession session) {
        User trainer = findTrainerFromSession(session);
        List<Schedule> schedules = scheduleService.findAllByStudentIdAndTrainerId(userId, trainer.getId());
        return schedules.stream().map(Schedule::toShortDto).toList();
    }


    //Admin endpoint, send bill to user by id in telegram
    @GetMapping("/user/send_bill/{id}")
    public Boolean sendBill(@PathVariable Long id, HttpSession session) {//todo check if it's trainer's student
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
            if (trainer.getMessenger() == Messenger.TELEGRAM) {
                bot.send(trainer.getChatId(), "Bill sent to " + student.getName() + ":\n" + response);
            }
            //todo не отправлять в телеграмм ученика, но сделать чат в программе и там отправлять
            return true;
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to send bill to user id " + id + ": " + e.getMessage());
        }

    }

    private User findTrainerFromSession(HttpSession session) {
        Long trainerId = (Long) session.getAttribute("USER_ID");
        return userService.findById(trainerId);
    }

    // Admin endpoint, get all users short info for the users list
    @GetMapping("/users")
    public List<UserShortDTO> getUsers(HttpSession session) {
        User trainer = findTrainerFromSession(session);
        log.info("trainer id=" + trainer.getId() + "trainer name " + trainer.getName() + " requested students list");
        List<User> students = userService.findAllByRoleInAndTrainerId(List.of(UserRole.BY_REQUEST, UserRole.PERMANENT, UserRole.TRAINER, UserRole.GROUP), trainer.getId());
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
    public PaymentDTO getBalances(@PathVariable Long id, HttpSession session) {
        User trainer = findTrainerFromSession(session);
        return lessonService.findBalanceByStudentIdAndTrainerId(id, trainer.getId());
    }

    // Admin endpoint, register payment from user by id and sum
    @PostMapping("/payment/{id}/{sum}")
    @Transactional
    public PaymentDTO paymentReceived(@PathVariable Long id, @PathVariable int sum, HttpSession session) {
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
    public UserFullDTO updateUser(@Valid @RequestBody UserUpdateByTrainerDto userUpdateByTrainerDto, HttpSession session) {

        User updatedUser = userService.updateByTrainer(userUpdateByTrainerDto);
        User trainer = findTrainerFromSession(session);

        List<Schedule> oldSchedules = scheduleService.findAllByStudentIdAndTrainerId(updatedUser.getId(), trainer.getId());

        User savedUser = userService.findByIdWithInfo(updatedUser.getId());
        return UserFullDTO.toFullDTO(savedUser, oldSchedules);
    }

    @DeleteMapping("user/schedule_delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSchedule(@PathVariable Long id, HttpSession session) {
        User trainer = findTrainerFromSession(session);
        Schedule scheduleToDelete = scheduleService.findById(id);
        if (scheduleToDelete.getTrainerId() != trainer.getId()) {
            throw new RuntimeException("Schedule id=" + id + " does not belong to trainer " + trainer.getName());
        }
        scheduleService.deleteSchedule(scheduleToDelete);
    }

    @PostMapping("user/schedule_add/{currentStudentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addSchedule(@PathVariable Long currentStudentId, @Validated @RequestBody ScheduleShortDTO newSchedule, HttpSession session) {
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
    public LessonFullDTO markLessonAsCompleted(@PathVariable Long id, @RequestBody(required = false) String newInfo, HttpSession session) {
        if (newInfo != null && !newInfo.isBlank()) {
            Lesson lesson = lessonService.findById(id);
            NewInfoDTO newInfoDTO = new NewInfoDTO(lesson.getStudent().getId(), newInfo);
            User trainer = findTrainerFromSession(session);
            infoService.saveFromNewInfoDTO(newInfoDTO, trainer.getId());
        }
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
        Lesson updatedLesson = calendarEventService.changeLessonTime(lessonUpdateDto.getStartTime(), newEndTime, lessonToUpdate);
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

    @GetMapping("/telegram_link")
    public Map<String,String> getTelegramLink(HttpSession session) {
        User trainer = findTrainerFromSession(session);
        String response= telegramEnterCodeService.createNewLink(trainer.getId());
        log.info(response);
        return Map.of("link", response);
    }

    @GetMapping("/subscription_types")
    public List<SubscriptionTypeStringDTO> getSubscriptionTypes(HttpSession session) {
        User trainer = findTrainerFromSession(session);
        return subscriptionTypeService.getSubscriptionTypesByTrainer(trainer.getId())
                .stream()
                .map(SubscriptionTypeStringDTO::new)
                .toList();
    }

    @PostMapping("/subscription_type/new")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addNewSubscriptionType(@Valid @RequestBody SubscriptionTypeNewDTO subscriptionTypeNewDTO, HttpSession session) {
        User trainer = findTrainerFromSession(session);
        subscriptionTypeService.saveNewSubscriptionType(subscriptionTypeNewDTO, trainer);
    }

    @PutMapping("/subscription_type/{id}/unactive")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSubscriptionType(@PathVariable Long id, HttpSession session) {
        User trainer = findTrainerFromSession(session);
        subscriptionTypeService.deleteSubscriptionType(id, trainer);
    }

    @GetMapping("/lesson/{id}/send_invitation")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sendLessonInvitation(@PathVariable Long id, HttpSession session) {
        User trainer = findTrainerFromSession(session);
        Lesson lesson = lessonService.findById(id);
        if (!lesson.getTrainer().getId().equals(trainer.getId())) {
            throw new RuntimeException("Lesson id=" + id + " does not belong to trainer " + trainer.getName());
        }
        try {
            lessonService.sendLessonInvitationalEmail(lesson);
        } catch (RuntimeException e) {
            log.error("Failed to send lesson invitation for lesson id=" + id + " by trainer " + trainer.getName() + ": " + e.getMessage());
            throw new RuntimeException("Failed to send lesson invitation: " + e.getMessage());
        }
    }

    @GetMapping("/subscriptions")
    public List<StudentSubscriptionDTO> getSubscriptionByTrainer(HttpSession session) {
        User trainer = findTrainerFromSession(session);
        List<StudentSubscription> subscriptions = subscriptionService.getSubscriptionByTrainerId(trainer.getId());
        return subscriptions.stream().map(subscription -> new StudentSubscriptionDTO(subscription, trainer.getRole())).toList();
    }

    @GetMapping("/subscription/{id}")
    public StudentSubscriptionFullDTO getSubscriptionById(@PathVariable Long id, HttpSession session) {
        User trainer = findTrainerFromSession(session);
        return lessonService.getStudentSubscriptionFullDTO(id, trainer.getId());
    }

    @PutMapping("/subscription/{id}/lesson/{lessonId}/remove")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeLessonFromSubscription(@PathVariable Long id, @PathVariable Long lessonId, HttpSession session) {
 //todo (удалить линку и вернуть +1 оставшихся уроков, если экспайред, то вернуть актив)
    }

    @DeleteMapping("/subscription/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSubscription(@PathVariable Long id, HttpSession session) {
        User trainer = findTrainerFromSession(session);
        subscriptionService.deleteSubscription(id, trainer.getId());
    }

    @PutMapping("/subscription/{id}/change")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeSubscription(@PathVariable Long id, @RequestBody ChangeSubscriptionDTO changeDto, HttpSession session) {
//todo
    }

    @PutMapping("/subscription/{id}/paid")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markSubscriptionAsPaid(@PathVariable Long id, HttpSession session) {
        User trainer = findTrainerFromSession(session);
        paymentService.markSubscriptionPaid(id, trainer.getId());
    }

}
