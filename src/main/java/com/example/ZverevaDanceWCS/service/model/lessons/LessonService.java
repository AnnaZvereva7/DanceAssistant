package com.example.ZverevaDanceWCS.service.model.lessons;

import com.example.ZverevaDanceWCS.service.googleCalendar.GoogleCalendarService;
import com.example.ZverevaDanceWCS.service.model.mailService.LessonMailService;
import com.example.ZverevaDanceWCS.service.model.exception.NotFoundException;
import com.example.ZverevaDanceWCS.service.model.exception.UnavailableTimeException;
import com.example.ZverevaDanceWCS.service.model.payments.PaymentDTO;
import com.example.ZverevaDanceWCS.service.model.freeSlots.*;
import com.example.ZverevaDanceWCS.service.model.appCalendarEvent.TimeRequest;
import com.example.ZverevaDanceWCS.service.model.subscriptions.*;
import com.example.ZverevaDanceWCS.service.model.subscriptions.dto.StudentSubscriptionFullDTO;
import com.example.ZverevaDanceWCS.service.model.subscriptions.dto.StudentSubscriptionNewDTO;
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
    private final LessonMailService lessonMailService;
    private final SubscriptionTypeService subscriptionTypeService;
    private final SubscriptionService subscriptionService;

    public LessonService(LessonsRepository lessonsRepository, GoogleCalendarService calendarService, UserService userService,
                         TrainerStudentService trainerStudentService, FreeSlotService slotService, LessonMailService lessonMailService,
                         SubscriptionTypeService subscriptionTypeService, SubscriptionService subscriptionService) {
        this.lessonsRepository = lessonsRepository;
        this.calendarService = calendarService;
        this.userService = userService;
        this.trainerStudentService = trainerStudentService;
        this.slotService = slotService;
        this.lessonMailService = lessonMailService;
        this.subscriptionTypeService = subscriptionTypeService;
        this.subscriptionService = subscriptionService;
    }

    public List<Lesson> findByStatusAndTrainerId(LessonStatus status, Long trainerId) {
        return lessonsRepository.findByStatusAndTrainerId(status, trainerId)
                .stream()
                .sorted(Comparator.comparing(Lesson::getStartTime))
                .collect(Collectors.toList());
    }

    public Lesson findById(Long id) {
        if (lessonsRepository.findById(id).isPresent()) {
            return lessonsRepository.findById(id).get();
        } else {
            throw new NotFoundException("Lesson with id =" + id + " not found.");
        }
    }

    //todo баланс должен быть тоже разделен по преподавателям, а не общий по ученику
    public String paymentToLessons(Long studentId, int sum, Long trainerId) {
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

    public List<Lesson> findByStatusAndStudentId(LessonStatus status, Long id) {
        return lessonsRepository.findByStatusAndStudentId(status, id);
    }

    public List<Lesson> findByStatusInAndStudentIdAndTrainerId(List<LessonStatus> statuses, Long studentId, Long trainerId) {
        return lessonsRepository.findByStatusInAndStudentIdAndTrainerId(statuses, studentId, trainerId);
    }

    public List<Lesson> findByStatusInAndStudentId(List<LessonStatus> statuses, Long id) {
        return lessonsRepository.findByStatusInAndStudentId(statuses, id);
    }

    public List<Lesson> findByStatusAndStudentIdAndTrainerId(LessonStatus status, Long studentId, Long trainerId) {
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
        if (lesson.getStudent().getEmail() != null && !lesson.getStudent().getEmail().isBlank()) {
            log.info("отправка письма");
            lessonMailService.sendLessonCreated(lesson);
        } else {
            log.info("Calendar email(creation) was not sent for lesson {}: student has no email", lesson.getId());
        }
        return lessonsRepository.save(lesson);
    }

    @Transactional
    public Lesson updateLesson(Lesson lesson) { //change lesson time
        if (lesson.getGoogleEventId() != null) {
            calendarService.updateEvent(lesson.getGoogleEventId(), lesson.getTitle(), lesson.getStudent().getAdditionalInfo(), lesson.getStartTime(), lesson.getEndTime());
        }
        if (lesson.getStudent().getEmail() != null && !lesson.getStudent().getEmail().isBlank()) {
            lessonMailService.sendLessonChange(lesson);
        } else {
            log.info("Calendar email(update) was not sent for lesson {}: student has no email", lesson.getId());
        }
        return lessonsRepository.save(lesson);
    }

    public void updateLessonInBase(Lesson lesson) {
        lessonsRepository.save(lesson);
    }

    @Transactional
    public Lesson cancelLesson(Lesson lesson) {
        if (lesson.getStatus() == LessonStatus.PENDING_STUDENT_CONFIRMATION
                || lesson.getStatus() == LessonStatus.PLANNED
                || lesson.getStatus() == LessonStatus.PENDING_TRAINER_CONFIRMATION) {
            lesson.setStatus(LessonStatus.CANCELED);
            if (lesson.getStudent().getEmail() != null && !lesson.getStudent().getEmail().isBlank()) {
                lessonMailService.sendLessonCancel(lesson);
            } else {
                log.info("Calendar email(cancel) was not sent for lesson {}: student has no email", lesson.getId());
            }
            if (lesson.getGoogleEventId() != null) {
                calendarService.deleteEvent(lesson.getGoogleEventId());
                lesson.setGoogleEventId(null);
            }
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

    public List<Lesson> findInPeriodNotCanceled(LocalDate startDate, LocalDate endDate, Long trainerId) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atStartOfDay().plusDays(1);
        return lessonsRepository.findByStartTimeBetweenAndTrainerIdAndStatusNot(startDateTime, endDateTime, trainerId, LessonStatus.CANCELED)
                .stream()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<Lesson> findByStudentAndPeriod(Long studentId, LocalDate startDate, LocalDate endDate) {
        return lessonsRepository.findByStudentIdAndStartTimeBetween(studentId, startDate.atStartOfDay(), endDate.atStartOfDay().plusDays(1));
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
        User student = lesson.getStudent();
        //todo check subscription and reduce remaining lessons if subscription exists
        StudentSubscription subscription = subscriptionService.StudentActiveSubscriptionOrNull(student.getId(), lesson.getTrainer().getId());
        if (subscription!=null) {
            lesson.setStatus(LessonStatus.PAID);
            updateLessonInBase(lesson);
            subscriptionService.newLinkCompletedLesson(lesson, subscription);
        } else {
            lesson.setCostAndForPayment();
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
        }
        return lesson;
    }

    public List<Lesson> findPassedNotCompletedLessons() {
        return lessonsRepository.findByStatusInAndStartTimeBefore(List.of(LessonStatus.PENDING_STUDENT_CONFIRMATION, LessonStatus.PLANNED), LocalDateTime.now());
    }

    public List<Lesson> findByStudentAndDateAfter(Long studentId, LocalDateTime dateTime) {
        return lessonsRepository.findByStudentIdAndStartTimeAfter(studentId, dateTime);
    }

    public List<Lesson> findByStatusInAndTrainerId(List<LessonStatus> statuses, Long trainerId) {
        return lessonsRepository.findByStatusInAndTrainerId(statuses, trainerId)
                .stream()
                .sorted(Comparator.comparing(Lesson::getStartTime))
                .collect(Collectors.toList());
    }

    public List<PaymentDTO> findAllBalanceByTrainer(Long trainerId) {
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

    public PaymentDTO findBalanceByStudentId(Long id) {
        User student = userService.findById(id);
        List<Lesson> lessons = findByStatusAndStudentId(LessonStatus.COMPLETED, student.getId())
                .stream()
                .sorted()
                .toList();
        return new PaymentDTO().toPaymentDTO(student, lessons);
    }

    public PaymentDTO findBalanceByStudentIdAndTrainerId(Long studentId, Long trainerId) {
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
        if (lesson.getStatus() != LessonStatus.PENDING_STUDENT_CONFIRMATION && lesson.getStatus() != LessonStatus.PENDING_TRAINER_CONFIRMATION) {
            throw new RuntimeException("Lesson in this status cannot be confirmed");
        }
        if (lesson.getStatus() == status) {
            lesson.setStatus(LessonStatus.PLANNED);
            lesson = lessonsRepository.save(lesson);
            return lesson;
        } else {
            throw new NotFoundException("Have no lesson to confirm");
        }
    }

    public Lesson createLessonFromStudentCalendar(TimeRequest timeRequest, User trainer, User student) {
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
            throw new UnavailableTimeException("This time is not available");
        }
    }

    public Lesson createLessonFromTrainerCalendar(TimeRequest timeRequest, User trainer, User student) {
        Lesson newLesson = new Lesson();
        newLesson.setStudent(student);
        newLesson.setTrainer(trainer);
        newLesson.setStartTime(timeRequest.getStart());
        newLesson.setEndTime(timeRequest.getEnd());
        newLesson.setStatus(LessonStatus.PENDING_STUDENT_CONFIRMATION);
        newLesson.setTitle(student.getName() + " WCS lesson");
        newLesson.setDurationMin((int) Duration.between(newLesson.getStartTime(), newLesson.getEndTime()).toMinutes());
        return saveNewLesson(newLesson);
    }

    public List<Lesson> getLessonsByStudentIdAndDateBetween(Long studentId, LocalDateTime startDate, LocalDateTime endDate) {
        return lessonsRepository.getLessonsByStudentIdAndStartTimeBetween(studentId, startDate, endDate);
    }

    public List<Lesson> getLessonsWithFilter(Long studentId, LocalDate startDate, LocalDate endDate, LessonStatus status) {
        if (studentId != null) {
            if (startDate != null & endDate != null & status != null) {
                if (startDate.isAfter(endDate)) {
                    throw new RuntimeException("Start date cannot be after end date");
                }
                return lessonsRepository.findByStudentIdAndStatusAndStartTimeBetween(studentId, status, startDate.atStartOfDay(), endDate.atStartOfDay().plusDays(1));
            } else if (startDate == null && endDate == null && status != null) {
                return lessonsRepository.findByStudentIdAndStatus(studentId, status);
            } else if (startDate != null && endDate == null && status != null) {
                return lessonsRepository.findByStudentIdAndStatusAndStartTimeAfter(studentId, status, startDate.atStartOfDay());
            } else if (startDate == null && endDate != null && status != null) {
                return lessonsRepository.findByStudentIdAndStatusAndStartTimeBefore(studentId, status, endDate.atStartOfDay().plusDays(1));
            } else if (startDate != null && endDate != null && status == null) {
                if (startDate.isAfter(endDate)) {
                    throw new RuntimeException("Start date cannot be after end date");
                }
                return lessonsRepository.findByStudentIdAndStartTimeBetween(studentId, startDate.atStartOfDay(), endDate.atStartOfDay().plusDays(1));
            } else if (startDate != null && endDate == null && status == null) {
                return lessonsRepository.findByStudentIdAndStartTimeAfter(studentId, startDate.atStartOfDay());
            } else if (startDate == null && endDate != null && status == null) {
                return lessonsRepository.findByStudentIdAndStartTimeBefore(studentId, endDate.atStartOfDay());
            } else if (startDate == null && endDate == null && status == null) {
                return lessonsRepository.findByStudentId(studentId);
            } else {
                throw new RuntimeException("Invalid filter parameters");
            }
        } else {
            if (startDate != null && endDate != null && status != null) {
                if (startDate.isAfter(endDate)) {
                    throw new RuntimeException("Start date cannot be after end date");
                }
                return lessonsRepository.findByStatusAndStartTimeBetween(status, startDate.atStartOfDay(), endDate.atStartOfDay().plusDays(1));
            } else if (startDate == null && endDate == null && status != null) {
                return lessonsRepository.findByStatus(status);
            } else if (startDate != null && endDate == null && status != null) {
                return lessonsRepository.findByStatusInAndStartTimeAfter(List.of(status), startDate.atStartOfDay());
            } else if (startDate == null && endDate != null && status != null) {
                return lessonsRepository.findByStatusInAndStartTimeBefore(List.of(status), endDate.atStartOfDay().plusDays(1));
            } else if (startDate != null && endDate != null && status == null) {
                if (startDate.isAfter(endDate)) {
                    throw new RuntimeException("Start date cannot be after end date");
                }
                return lessonsRepository.findByStartTimeBetween(startDate.atStartOfDay(), endDate.atStartOfDay().plusDays(1));
            } else if (startDate != null && endDate == null && status == null) {
                return lessonsRepository.findByStartTimeAfter(startDate.atStartOfDay());
            } else if (startDate == null && endDate != null && status == null) {
                return lessonsRepository.findByStartTimeBefore(endDate.atStartOfDay());
            } else if (startDate == null && endDate == null && status == null) {
                return lessonsRepository.findAll();
            } else {
                throw new RuntimeException("Invalid filter parameters");
            }
        }
    }

    public Boolean checkIfExistsUnfinishedLessons(Long userId) {
        List<Lesson> lessons = findByStatusInAndStudentId(List.of(LessonStatus.PENDING_TRAINER_CONFIRMATION,
                LessonStatus.PENDING_TRAINER_CONFIRMATION, LessonStatus.PLANNED, LessonStatus.COMPLETED), userId);
        return !lessons.isEmpty();
    }

    public void sendLessonInvitationalEmail(Lesson lesson) {
        if (lesson.getStudent().getEmail() != null && !lesson.getStudent().getEmail().isBlank()) {
            lessonMailService.sendLessonCreated(lesson);
        } else {
            log.info("Calendar email(invitation) was not sent for lesson {}: student has no email", lesson.getId());
        }
    }

    public StudentSubscription addNewSubscription(StudentSubscriptionNewDTO dto, Long userId) {
        User student = userService.findById(userId);
        SubscriptionType subscriptionType = subscriptionTypeService.findById(dto.getSubscriptionTypeId());
        User trainer =userService.findById(subscriptionType.getTrainerId());
        StudentSubscription subscription = new StudentSubscription();
        subscription.setTrainerName(trainer.getName());
        subscription.setStudentName(student.getName());
        subscription.setSubscriptionType(subscriptionType);
        subscription.setStudentId(student.getId());
        subscription.setStartDate(dto.getStartDate());
        subscription.setEndDate(dto.getStartDate().plusMonths(subscriptionType.getDurationMonths()));
        subscription.setRemainingLessons(subscriptionType.getAmountLessons());
        subscription.setCreatedAt(LocalDateTime.now());
        subscription.setStatus(SubscriptionStatus.UNPAID);
        subscription.setPrice(subscriptionType.getPrice());
        return subscriptionService.saveSubscription(subscription);
    }

    public StudentSubscriptionFullDTO getStudentSubscriptionFullDTO(Long subscriptionId, Long trainerId) {
        StudentSubscription subscription = subscriptionService.findById(subscriptionId);
        if(!subscription.getSubscriptionType().getTrainerId().equals(trainerId)) {
            throw new RuntimeException("You are not authorized to view this subscription");
        }
        List<Lesson> lessons = subscriptionService.getLessonsBySubscriptionId(subscriptionId);
        return new StudentSubscriptionFullDTO(subscription, lessons);
    }

    public Lesson changeLessonStatus(Long lessonId, LessonStatus oldStatus,LessonStatus newStatus, Long trainerId) {
        Lesson lesson = findById(lessonId);
        if(!lesson.getTrainer().getId().equals(trainerId)) {
            throw new RuntimeException("You are not authorized to change this lesson status");
        }
        if(lesson.getStatus()!=oldStatus){
            throw new RuntimeException("Lesson status is not "+oldStatus);
        }
        if(oldStatus==LessonStatus.COMPLETED&&(newStatus==LessonStatus.PENDING_STUDENT_CONFIRMATION
                ||newStatus==LessonStatus.PLANNED)){
            lesson.setStatus(newStatus);
            lesson.setForPayment(lesson.getCost());
            updateLessonInBase(lesson);
            User student=lesson.getStudent();
            student.setBalance(student.getBalance()+lesson.getCost());
        } else if ((oldStatus==LessonStatus.PENDING_STUDENT_CONFIRMATION
                ||oldStatus==LessonStatus.PENDING_TRAINER_CONFIRMATION
                ||oldStatus==LessonStatus.PLANNED)&&newStatus==LessonStatus.CANCELED_WITH_CHARGE){
            lesson=lessonCompleted(lesson);
            lesson.setStatus(LessonStatus.CANCELED_WITH_CHARGE);
            updateLessonInBase(lesson);
        } else {
            throw new RuntimeException("Invalid status change from "+oldStatus+" to "+newStatus);
        }
        return lesson;
    }


}
