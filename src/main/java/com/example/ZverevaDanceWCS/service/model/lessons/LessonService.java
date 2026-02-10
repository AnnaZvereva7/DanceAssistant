package com.example.ZverevaDanceWCS.service.model.lessons;

import com.example.ZverevaDanceWCS.service.calendar.GoogleCalendarService;
import com.example.ZverevaDanceWCS.service.model.calendarEvent.CalendarEventService;
import com.example.ZverevaDanceWCS.service.model.exception.ExceptionForAdmin;
import com.example.ZverevaDanceWCS.service.model.exception.NotFoundException;
import com.example.ZverevaDanceWCS.service.model.exception.UnavailableTimeExeption;
import com.example.ZverevaDanceWCS.service.model.payments.PaymentDTO;
import com.example.ZverevaDanceWCS.service.model.freeSlots.*;
import com.example.ZverevaDanceWCS.service.model.calendarEvent.TimeRequest;
import com.example.ZverevaDanceWCS.service.model.trainerStudentLink.TrainerStudentService;
import com.example.ZverevaDanceWCS.service.model.user.User;
import com.example.ZverevaDanceWCS.service.model.user.UserRole;
import com.example.ZverevaDanceWCS.service.model.user.UserService;
import org.springframework.stereotype.Service;

import java.time.Duration;
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
    private final FreeSlotService slotService;


    public LessonService(LessonsRepository lessonsRepository, GoogleCalendarService calendarService, UserService userService,
                         TrainerStudentService trainerStudentService, FreeSlotService slotService) {
        this.lessonsRepository = lessonsRepository;
        this.calendarService = calendarService;
        this.userService = userService;
        this.trainerStudentService = trainerStudentService;
        this.slotService = slotService;

    }

    public List<Lesson> findByStatusAndTrainerId(LessonStatus status, int trainerId) {
        return lessonsRepository.findByStatusAndTrainerId(status, trainerId)
                .stream()
                .sorted(Comparator.comparing(Lesson::getStartTime))
                .collect(Collectors.toList());
    }

    public Lesson findById(Long id) {
        if(lessonsRepository.findById(id).isPresent()) {
            return lessonsRepository.findById(id).get();
        } else {
            throw new NotFoundException("Lesson with id =" +id+ " not found.");
        }
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

    public List<Lesson> findByStatusInAndStudentId(List<LessonStatus> statuses, int id) {
        return lessonsRepository.findByStatusInAndStudentId(statuses, id);
    }

    public List<Lesson> findByStatusAndStudentIdAndTrainerId(LessonStatus status, int studentId, int trainerId) {
        return lessonsRepository.findByStatusAndStudentIdAndTrainerId(status, studentId, trainerId);
    }

    @Transactional  //todo не создаются урокаи после ошибки с датой раньше now только для учеников
    public Lesson saveNewLesson(Lesson lesson) { //урок сохраняется в базу, сохраняется связб учитель-тренер в базу, удаляется free слот если был, создается гугл событие
        Lesson newLesson = lessonsRepository.save(lesson);
        lesson.setId(newLesson.getId());
        trainerStudentService.saveLink(newLesson.getTrainer(), newLesson.getStudent());
        lesson.setTitle(newLesson.getTitle() + " (id=" + lesson.getId() + ")");
        slotService.bookPartOfFreeSlot(lesson.getStartTime(), lesson.getEndTime(), lesson.getTrainer().getId());
        String eventId = calendarService.addEvent(lesson.getTitle(), "", lesson.getStartTime(), lesson.getEndTime());
        lesson.setGoogleEventId(eventId);
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
        if (lesson.getStatus() == LessonStatus.PENDING_STUDENT_CONFIRMATION
                || lesson.getStatus() == LessonStatus.PLANNED
                || lesson.getStatus() == LessonStatus.PENDING_TRAINER_CONFIRMATION) {
            lesson.setStatus(LessonStatus.CANCELED);
            calendarService.deleteEvent(lesson.getGoogleEventId());
            lesson.setGoogleEventId(null);
            slotService.addFreeTimeSlot(lesson.getStartTime(), lesson.getEndTime(), lesson.getTrainer().getId());
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
        return lessonsRepository.findByStatusInAndStartTimeBefore(List.of(LessonStatus.PENDING_STUDENT_CONFIRMATION, LessonStatus.PLANNED), LocalDateTime.now());
    }

    public List<Lesson> findByStudentAndDateAfter(int studentId, LocalDateTime dateTime) {
        return lessonsRepository.findByStudentIdAndStartTimeAfter(studentId, dateTime);
    }

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

    @Transactional
    public Lesson confirmLesson(Long lessonId, LessonStatus status) {
        Lesson lesson = findById(lessonId);
        if (lesson.getStatus() == status) {
            lesson.setStatus(LessonStatus.PLANNED);
            lesson = lessonsRepository.save(lesson);
            return lesson;
        } else {
            throw new NotFoundException("Have no lesson to confirm");
        }
    }

    public Lesson createLessonFromCalendar(TimeRequest timeRequest, User trainer, User student) {
        if (slotService.checkIfSlotFree(timeRequest.getStart(), timeRequest.getEnd(), trainer.getId())) {
            Lesson newLesson = new Lesson();
            newLesson.setStudent(student);
            newLesson.setTrainer(trainer);
            newLesson.setStartTime(timeRequest.getStart());
            newLesson.setEndTime(timeRequest.getEnd());
            newLesson.setStatus(LessonStatus.PENDING_TRAINER_CONFIRMATION);
            newLesson.setTitle(student.getName() + " WCS lesson");
            newLesson.setDurationMin((int) Duration.between(newLesson.getStartTime(), newLesson.getEndTime()).toMinutes());
            return saveNewLesson(newLesson);
        } else {
            throw new UnavailableTimeExeption("This time is not available");
        }
    }


}
