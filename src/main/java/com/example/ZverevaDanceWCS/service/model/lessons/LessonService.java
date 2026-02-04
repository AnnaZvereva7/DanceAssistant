package com.example.ZverevaDanceWCS.service.model.lessons;

import com.example.ZverevaDanceWCS.service.calendar.GoogleCalendarService;
import com.example.ZverevaDanceWCS.service.model.exception.ExceptionForAdmin;
import com.example.ZverevaDanceWCS.service.model.exception.NotFoundException;
import com.example.ZverevaDanceWCS.service.model.payments.PaymentDTO;
import com.example.ZverevaDanceWCS.service.model.trainerStudentLink.TrainerStudentService;
import com.example.ZverevaDanceWCS.service.model.user.User;
import com.example.ZverevaDanceWCS.service.model.user.UserRole;
import com.example.ZverevaDanceWCS.service.model.user.UserService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class LessonService {

    private final LessonsRepository lessonsRepository;
    private final GoogleCalendarService calendarService;
    private final UserService userService;
    private final TrainerStudentService trainerStudentService;

    public LessonService(LessonsRepository lessonsRepository, GoogleCalendarService calendarService, UserService userService, TrainerStudentService trainerStudentService) {
        this.lessonsRepository = lessonsRepository;
        this.calendarService = calendarService;
        this.userService = userService;
        this.trainerStudentService = trainerStudentService;
    }

    public List<Lesson> findByStatusAndTrainerId(LessonStatus status, int trainerId) {
        return lessonsRepository.findByStatusAndTrainerId(status, trainerId)
                .stream()
                .sorted(Comparator.comparing(Lesson::getStartTime))
                .collect(Collectors.toList());
    }

    public Lesson findById(int id) {
        return lessonsRepository.findById(id);
    }

    //todo баланс должен быть тоже разделен по преподавателям, а не общий по ученику
    public String paymentToLessons(int studentId, int sum, int trainerId) {
        List<Lesson> lessons = lessonsRepository.findByStatusAndStudentIdAndTrainerId(LessonStatus.COMPLETED, studentId, trainerId)
                .stream()
                .sorted(Comparator.comparing(Lesson::getStartTime))
                .collect(Collectors.toList());
        String answer = "";
        int paidLessons = 0;
        while (sum > 0 && lessons.size() > paidLessons) {
            Lesson lesson = lessons.get(paidLessons);
            if (lesson.getForPayment() <= sum) {
                sum -= lesson.getForPayment();
                lesson.setForPayment(0);
                paidLessons += 1;
                lesson.setStatus(LessonStatus.PAID);
                lessonsRepository.save(lesson);
            } else {
                lesson.setForPayment(lesson.getForPayment() - sum);
                lessonsRepository.save(lesson);
                sum = 0;
                answer = answer + "lesson with id=" + lesson.getId() + " partially paid ";
            }
        }
        answer = answer + paidLessons + " lessons fully paid";
        return answer;
    }

    public List<Lesson> findByStatusAndStudentId(LessonStatus status, int id) {
        return lessonsRepository.findByStatusAndStudentId(status, id);
    }
    public List<Lesson> findByStatusAndStudentIdAndTrainerId(LessonStatus status, int studentId, int trainerId) {
        return lessonsRepository.findByStatusAndStudentIdAndTrainerId(status, studentId, trainerId);
    }

    @Transactional
    public Lesson saveNewLesson(Lesson lesson) { //todo не создаются урокаи после ошибки с датой раньше now только для учеников
        Lesson newLesson = lessonsRepository.save(lesson);
        trainerStudentService.saveLink(newLesson.getTrainer(), newLesson.getStudent());
        newLesson.setTitle(newLesson.getTitle() + " (id=" + lesson.getId() + ")");
        String eventId = calendarService.addEvent(lesson.getTitle(), "", lesson.getStartTime(), lesson.getEndTime());
        lesson.setGoogleEventId(eventId);
        trainerStudentService.saveLink(lesson.getTrainer(), lesson.getStudent());
        return lessonsRepository.save(lesson);
    }

    @Transactional
    public Lesson updateLesson(Lesson lesson) {
        calendarService.updateEvent(lesson.getGoogleEventId(), lesson.getTitle(), lesson.getStudent().getAdditionalInfo(), lesson.getStartTime(), lesson.getEndTime());
        return lessonsRepository.save(lesson);
    }

    public Lesson updateLessonInBase(Lesson lesson) {
        return lessonsRepository.save(lesson);
    }

    @Transactional
    public Lesson cancelLesson(Lesson lesson) {
        if (lesson.getStatus() == LessonStatus.TO_CONFIRM || lesson.getStatus() == LessonStatus.PLANNED) {
            lesson.setStatus(LessonStatus.CANCELED);
            calendarService.deleteEvent(lesson.getGoogleEventId());
            lesson.setGoogleEventId(null);
            return lessonsRepository.save(lesson);
        } else {
            throw new NotFoundException("Урок в статусе " + lesson.getStatus() + " не может быть отменен");
        }

    }

    public List<Lesson> findByDate(LocalDate date) {
        LocalDateTime startDate = date.atStartOfDay();
        LocalDateTime endDate = startDate.plusDays(1);
        return lessonsRepository.findByStartTimeBetween(startDate, endDate);
    }

    public List<Lesson> findInPeriod(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atStartOfDay().plusDays(1);
        return lessonsRepository.findByStartTimeBetween(startDateTime, endDateTime)
                .stream()
                .sorted(Comparator.comparing(Lesson::getStartTime))
                .collect(Collectors.toList());
    }

    public List<Lesson> findInPeriodNotCanceled(LocalDate startDate, LocalDate endDate, int trainerId) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atStartOfDay().plusDays(1);
        return lessonsRepository.findByStartTimeBetweenAndTrainerIdAndStatusNot(startDateTime, endDateTime, trainerId, LessonStatus.CANCELED)
                .stream()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<Lesson> findByStudentAndPeriod(int studentId, LocalDate startDate, LocalDate endDate) {
        return lessonsRepository.findByStudentIdAndStartTimeBetween(studentId, startDate.atStartOfDay(), endDate.atStartOfDay().plusDays(1));
    }

    public Lesson findByStudentDate(LocalDate date, int studentId) {
        LocalDateTime startDate = date.atStartOfDay();
        LocalDateTime endDate = startDate.plusDays(1);
        List<Lesson> lessons = lessonsRepository.findByStudentIdAndStartTimeBetween(studentId, startDate, endDate);
        if (lessons.isEmpty()) {
            throw new NotFoundException("No lessons on this date");
        } else if (lessons.size() == 1) {
            return lessons.get(0);
        } else {
            throw new ExceptionForAdmin("More then one lesson on one date");
        }
    }

    public String lessonsToBill(List<Lesson> lessons) {
        String answer = "Completed lessons:\n";
        int total = 0;
        for (Lesson lesson : lessons) {
            answer = answer + lesson.stringForBill();
            total += lesson.getForPayment();
        }
        answer = answer + "To pay: " + total + " EUR";
        return answer;
    }

    public Lesson lessonCompleted(Lesson lesson) {
        lesson.setCostAndForPayment();
        User student = lesson.getStudent();
        int balance = student.getBalance();
        int forPay = lesson.getForPayment();
        student.setBalance(balance - forPay);
        if (balance > 0 && balance < forPay) {
            lesson.setForPayment(forPay - balance);
            lesson.setStatus(LessonStatus.COMPLETED);
        } else if (balance >= forPay) {
            lesson.setForPayment(0);
            lesson.setStatus(LessonStatus.PAID);
        } else {
            lesson.setStatus(LessonStatus.COMPLETED);
        }
        userService.saveUser(student);
        updateLessonInBase(lesson);
        return lesson;
    }

    public boolean existByStudentDate(LocalDate date, int studentId) {
        LocalDateTime startDate = date.atStartOfDay();
        LocalDateTime endDate = startDate.plusDays(1);
        return lessonsRepository.existsByStudentIdAndStartTimeBetween(studentId, startDate, endDate);
    }

    public List<Lesson> findPassedNotCompletedLessons() {
        return lessonsRepository.findByStatusInAndStartTimeBefore(List.of(LessonStatus.TO_CONFIRM, LessonStatus.PLANNED), LocalDateTime.now());
    }

    public List<Lesson> findByStudentAndDateAfter(int studentId, LocalDateTime dateTime) {
        return lessonsRepository.findByStudentIdAndStartTimeAfter(studentId, dateTime);
    }

    public void addExistedToGoogle() {
        List<Lesson> lessons = lessonsRepository.findByStatusInAndStartTimeAfter(List.of(LessonStatus.TO_CONFIRM, LessonStatus.PLANNED), LocalDateTime.now());
        for (Lesson lesson : lessons) {
            String event_id = calendarService.addEvent(lesson.getTitle(), lesson.getStudent().getAdditionalInfo(), lesson.getStartTime(), lesson.getEndTime());
            lesson.setGoogleEventId(event_id);
            lessonsRepository.save(lesson);
        }
    }

//    public Lesson addLessonBySchedule(User student) { //todo!
//        DayOfWeek day = student.getScheduleDay();
//        log.info("day of week " + day.name());
//        LocalTime time = student.getScheduleTime();
//        LocalDate date = LocalDate.now().with(TemporalAdjusters.nextOrSame(day));
//        if (!existByStudentDate(date, student.getId())) {
//            return saveNewLesson(new Lesson(student, LocalDateTime.of(date, time), LessonStatus.TO_CONFIRM));
//        } else {
//            throw new RuntimeException("Lesson already exists for student " + student.getName());
//        }
//    }

    public List<Lesson> findByStudentIdAndStatus(int studentId, LessonStatus status) {
        return lessonsRepository.findByStudentIdAndStatus(studentId, status);
    }


    public List<Lesson> findByStatusInAndTrainerId(List<LessonStatus> statuses, int trainerId) {
        return lessonsRepository.findByStatusInAndTrainerId(statuses, trainerId)
                .stream()
                .sorted(Comparator.comparing(Lesson::getStartTime))
                .collect(Collectors.toList());
    }

    public List<PaymentDTO> findAllBalanceByTrainer(int trainerId) {
        List<PaymentDTO> payments = new ArrayList<>();
        List<User> users = userService.findAllByRoleInAndTrainerId(List
                .of(UserRole.BY_REQUEST, UserRole.PERMANENT, UserRole.GROUP), trainerId);
        for (User student : users) {
            List<Lesson> lessons = findByStatusAndStudentId(LessonStatus.COMPLETED, student.getId())
                    .stream()
                    .sorted()
                    .toList();
            payments.add(new PaymentDTO().toPaymentDTO(student, lessons));
        }
        return payments;
    }

    public PaymentDTO findBalanceByStudentId(int id) {
        User student = userService.findById(id);
        List<Lesson> lessons = findByStatusAndStudentId(LessonStatus.COMPLETED, student.getId())
                .stream()
                .sorted()
                .toList();
        return new PaymentDTO().toPaymentDTO(student, lessons);
    }

    public PaymentDTO findBalanceByStudentIdAndTrainerId(int studentId, int trainerId) {
        User student = userService.findById(studentId);
        List<Lesson> lessons = findByStatusAndStudentIdAndTrainerId(LessonStatus.COMPLETED, student.getId(), trainerId)
                .stream()
                .sorted()
                .toList();
        return new PaymentDTO().toPaymentDTO(student, lessons);
    }

}
