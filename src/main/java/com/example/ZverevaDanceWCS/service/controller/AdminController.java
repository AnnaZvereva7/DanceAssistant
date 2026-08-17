package com.example.ZverevaDanceWCS.service.controller;

import com.example.ZverevaDanceWCS.service.model.lessons.Lesson;
import com.example.ZverevaDanceWCS.service.model.lessons.LessonService;
import com.example.ZverevaDanceWCS.service.model.lessons.LessonStatus;
import com.example.ZverevaDanceWCS.service.model.lessons.lessonDTO.LessonFullDTO;
import com.example.ZverevaDanceWCS.service.model.lessons.lessonDTO.LessonShortDTO;
import com.example.ZverevaDanceWCS.service.model.payments.Payment;
import com.example.ZverevaDanceWCS.service.model.payments.PaymentService;
import com.example.ZverevaDanceWCS.service.model.payments.TransactionDataDao;
import com.example.ZverevaDanceWCS.service.model.trainerStudentLink.TrainerStudentService;
import com.example.ZverevaDanceWCS.service.model.user.User;
import com.example.ZverevaDanceWCS.service.model.user.UserService;
import com.example.ZverevaDanceWCS.service.model.user.userDTO.UserAdminDto;
import com.example.ZverevaDanceWCS.service.model.user.userDTO.UserShortDTO;
import com.example.ZverevaDanceWCS.service.model.user.userDTO.UserUpdateByTrainerDto;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    final UserService userService;
    final TrainerStudentService trainerStudentService;
    final LessonService lessonService;
    final PaymentService paymentService;

    public AdminController(UserService userService, TrainerStudentService trainerStudentService, LessonService lessonService, PaymentService paymentService) {
        this.userService = userService;
        this.trainerStudentService = trainerStudentService;
        this.lessonService = lessonService;
        this.paymentService = paymentService;
    }

    @GetMapping("users")
    public List<UserShortDTO> getAllUsersForAdmin() {
        List<User> users = userService.findAll();
        return users.stream().map(user -> new UserShortDTO(user.getId(), user.getName(), user.getRole().toString())).toList();
        //todo на страницу admin.html возвращается ссылочный список формата "имя-статус"
    }

    @PutMapping("change_user")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeUserByAdmin(@Valid @RequestBody UserUpdateByTrainerDto userUpdateByAdminDto) {
        userService.updateByTrainer(userUpdateByAdminDto);
        //todo изменить почту, в историю (id,name, email, role, messenger))

    }

    @DeleteMapping("delete_user")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUserByAdmin(@RequestParam Long userId) { //user mark as to_delete(delete the mail and all information, keep info about finished lessons), if user has unfinished lessons - throw exception
        if (lessonService.checkIfExistsUnfinishedLessons(userId)) {
            throw new RuntimeException("The user has unfinished lessons and cannot be deleted.");
        }
        userService.deleteUserByAdmin(userId);
    }


    @GetMapping("trainers")
    public List<UserShortDTO> findAllTrainers () {
        return userService.findAllTrainers().stream().map(UserShortDTO::toShortDTO).toList();
    }

    @PutMapping("add_trainer/{student_id}/{trainer_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addTrainerToUser(@PathVariable Long student_id, @PathVariable Long trainer_id) { //по кнопке "add trainer" всплывающее окно со списком всех тренеров
        User student = userService.findById(student_id);
        User trainer = userService.findById(trainer_id);
        trainerStudentService.saveLink(trainer, student);
        //todo в историю
    }

    //todo объединение пользователей в одного ()

    @GetMapping("user_info/{id}")
    public UserAdminDto getUserByIdForAdmin(@PathVariable Long id) {
        //todo пользователь с данными о преподах (name, email, role, trainers + operations history button)
        User user = userService.findById(id);
        List<User> trainers = trainerStudentService.getAllTrainersByStudent(id);
        return new UserAdminDto(user, trainers);
    }

    @GetMapping("user_transaction_history/{id}")
    public List<TransactionDataDao> getUserTransactionByIdForAdmin(@PathVariable Long id, @RequestParam(required = false) LocalDate startDate,
                                                                   @RequestParam(required = false) LocalDate endDate) {
        if (startDate == null) {
            LocalDate nowDate = LocalDate.now();
            int month = nowDate.getMonthValue();
            int year = nowDate.getYear();
            startDate = LocalDate.of(year, month, 1);
        }

        if (endDate == null) {
            LocalDate nowDate = LocalDate.now();
            int month = nowDate.getMonthValue();
            int year = nowDate.getYear();
            endDate = LocalDate.of(year, month, nowDate.lengthOfMonth());
        }

        if (startDate.isAfter(endDate)) {
            throw new RuntimeException("Start date cannot be after end date");
        }

        List<Lesson> lessons = lessonService.getLessonsByStudentIdAndDateBetween(id, startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
        List<Payment> payments = paymentService.findByStudentAndPeriod(id, startDate, endDate);

        List<TransactionDataDao> transactions = Stream.concat(
                        lessons.stream().map(lesson -> TransactionDataDao.fromLesson(lesson)),
                        payments.stream().map(payment -> TransactionDataDao.fromPayment(payment))
                ).sorted(Comparator.comparing(TransactionDataDao::getDate))
                .toList();
        return transactions;
    }

    @GetMapping ("lessons_filtered") //loadLessonsWithFilter
    public List<LessonShortDTO> findLessonsWithFilter (@RequestParam (required = false) Long studentId,
                                                       @RequestParam (required = false) LocalDate startDate,
                                                       @RequestParam (required = false) LocalDate endDate,
                                                       @RequestParam (required = false) LessonStatus status,
                                                       @RequestParam (required = false) Long lessonId) {
        if (lessonId!=null) {
            Lesson lesson=lessonService.findById(lessonId);
            return List.of(lesson).stream().map(LessonShortDTO::toShortDTO).toList();
        } else {
            return lessonService.getLessonsWithFilter(studentId, startDate, endDate, status).stream().map(LessonShortDTO::toShortDTO).toList();
        }
    }

    @GetMapping("lesson/{lessonId}")
    public LessonFullDTO findLessonById(@PathVariable Long lessonId) {
        return LessonFullDTO.toFullDTO(lessonService.findById(lessonId));
    }



}
