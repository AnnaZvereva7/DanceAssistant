package com.example.ZverevaDanceWCS.service.telegramBot;

import com.example.ZverevaDanceWCS.service.calendar.GoogleCalendarService;
import com.example.ZverevaDanceWCS.service.Constant;
import com.example.ZverevaDanceWCS.service.model.exception.CommandNotRecognizedException;
import com.example.ZverevaDanceWCS.service.model.exception.NotFoundException;
import com.example.ZverevaDanceWCS.service.model.lessons.LessonService;
import com.example.ZverevaDanceWCS.service.model.lessons.lessonDTO.LessonAdminDAO;
import com.example.ZverevaDanceWCS.service.model.lessons.LessonStatus;
import com.example.ZverevaDanceWCS.service.model.lessons.Lesson;
import com.example.ZverevaDanceWCS.service.model.payments.Payment;
import com.example.ZverevaDanceWCS.service.model.payments.PaymentService;
import com.example.ZverevaDanceWCS.service.model.user.studentInfo.InfoService;
import com.example.ZverevaDanceWCS.service.model.user.studentInfo.InfoStatus;
import com.example.ZverevaDanceWCS.service.model.user.studentInfo.StudentInfo;
import com.example.ZverevaDanceWCS.service.model.user.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TelegramTrainerService {

    final UserService userService;
    final LessonService lessonService;
    final PaymentService paymentService;
    final GoogleCalendarService calendarService;
    final InfoService infoService;

    @Autowired
    public TelegramTrainerService(UserService userService, LessonService lessonService, PaymentService paymentService,
                                  GoogleCalendarService calendarService, InfoService infoService) {
        this.userService = userService;
        this.lessonService = lessonService;
        this.paymentService = paymentService;
        this.calendarService = calendarService;
        this.infoService = infoService;
    }


    public String printMenuAdmin() {
        StringBuilder menu = new StringBuilder("You can use one of these commands: \n");
        menu.append("/student_list:[role] - (id, name, chat name, status) \n");
        menu.append("/planned_lesson\n");
        menu.append("/add_info:studentId-info\n");
        menu.append("/show_info:studentId\n");
        menu.append("/lesson_completed:lessonId - change lesson status+add cost \n");
        menu.append("/cancel_lesson:lessonId\n");
        menu.append("/send_bill:studentId\n");
        menu.append("/unpaid:[studentId]\n"); //список неоплаченных занятий студента
        menu.append("/balance\n");
        menu.append("/payment:id-sum\n");
        menu.append("/month_payments:2025-1\n");
        menu.append("/year_payments:2025\n");
        return menu.toString();
    }

    //month_payments:2025-1 - оплаты за месяц
    public String monthPaymentByTrainer(String[] addInfo, Long trainerChatId) {
        User trainer = userService.findByChatId(trainerChatId);
        if (addInfo != null && addInfo.length == 2) {
            int year = Integer.parseInt(addInfo[0]);
            int month = Integer.parseInt(addInfo[1]);
            List<Payment> payments = paymentService.findByMonthAndYearAndTrainerId(month, year, trainer.getId());
            int sum = 0;
            for (Payment payment : payments) {
                sum += payment.getSum();
            }
            return String.valueOf(sum);
        } else {
            throw new CommandNotRecognizedException();
        }
    }

    //year_payments:2025
    public String yearPaymentsByTrainer(String[] addInfo, Long trainerChatId) {
        User trainer = userService.findByChatId(trainerChatId);
        if (addInfo != null && addInfo.length == 1) {
            int year = Integer.parseInt(addInfo[0]);
            int sum = paymentService.findByYearAndTrainerId(year, trainer.getId()).stream().mapToInt(Payment::getSum).sum();
            return String.valueOf(sum);
        } else {
            throw new CommandNotRecognizedException();
        }
    }

    //add_info:studentId-info
    public HashMap<Long, String> addNewInfo(Long trainerChatId, String[] addInfo) {
        User trainer = userService.findByChatId(trainerChatId);
        HashMap<Long, String> responses = new HashMap<>();
        if (addInfo != null && addInfo.length == 2) {
            StudentInfo info = new StudentInfo();
            info.setStudentId(Integer.parseInt(addInfo[0]));
            info.setInfo(addInfo[1]);
            info.setDate(LocalDate.now());
            info.setStatus(InfoStatus.ACTUAL);
            info.setTrainerId(trainer.getId());
            User student = userService.findById(Integer.parseInt(addInfo[0]));
            if (student.getMessenger() == Messenger.TELEGRAM) {
                responses.put(student.getChatId(), "New information: " + info.toString());
                responses.put(trainerChatId,
                        "New information for " + userService.findById(Integer.parseInt(addInfo[0])).getName() + ": " + info.toString());
            }
        } else {
            throw new CommandNotRecognizedException();
        }
        return responses;
    }

    //show_info:studentId
    //show_info (for every actual info)
    public String showInfo(String[] addInfo, Long trainerChatId) {
        User trainer = userService.findByChatId(trainerChatId);
        if (addInfo != null && addInfo.length == 1) {
            User student = userService.findById(Integer.parseInt(addInfo[0]));
            return student.getName() + " info:\n"
                    + infoService.findByStudentAndTrainerActual(Integer.parseInt(addInfo[0]), trainer.getId());
        } else if (addInfo == null) {
            Map<Integer, String> infoMap = infoService.findAllByStatusAndTrainer(InfoStatus.ACTUAL, trainer.getId());
            String response = "";
            for (Map.Entry<Integer, String> entry : infoMap.entrySet()) {
                User student = userService.findById(entry.getKey());
                response = response + "\n\n" + student.getName() + " info:\n" + entry.getValue();
            }
            return response;
        } else {
            throw new CommandNotRecognizedException();
        }
    }

    //info_old:infoId
    public String infoChangeStatus(String[] addInfo) {
        if (addInfo != null && addInfo.length == 1) {
            StudentInfo info = infoService.findById(Integer.parseInt(addInfo[0]));
            info.setStatus(InfoStatus.OLD);
            infoService.save(info);
            return "Status of information changed";
        } else {
            throw new CommandNotRecognizedException();
        }
    }


    //planned_lesson
    public String plannedLessons(Long trainerChatId) {
        User trainer = userService.findByChatId(trainerChatId);
        String answer = "";
        List<Lesson> lessons = lessonService.findByStatusAndTrainerId(LessonStatus.PLANNED, trainer.getId());
        for (Lesson lesson : lessons) {
            answer = answer + lesson.getStartTime().format(Constant.formatterDayTime) + " - "
                    + lesson.getStudent().getName() + " - "
                    + lesson.getStatus() + " ("
                    + lesson.getId() + ");\n";
        }
        return answer;
    }


    public HashMap<Long, String> sendBill(String[] addInfo, Long trainerChatId) { //send_bill:studentId
        User trainer = userService.findByChatId(trainerChatId);
        HashMap<Long, String> response = new HashMap<>();
        if (addInfo != null && addInfo.length == 1) {
            User student = userService.findById(Integer.parseInt(addInfo[0]));
            List<Lesson> lessons = lessonService.findByStatusAndStudentIdAndTrainerId(LessonStatus.COMPLETED, student.getId(), trainer.getId()).stream()
                    .sorted()
                    .collect(Collectors.toList());
            String answer = lessonService.lessonsToBill(lessons);
            if (student.getMessenger() == Messenger.TELEGRAM) {
                response.put(student.getChatId(), answer);
            }
            response.put(trainerChatId, student.getName() + "\n" + answer);
        } else {
            throw new CommandNotRecognizedException();
        }
        return response;
    }

    public HashMap<Long, String> cancelLesson(Long trainerChatId, String[] addInfo) {
        HashMap<Long, String> responses = new HashMap<>();
        if (addInfo != null && addInfo.length == 1) {
            Lesson lesson = lessonService.findById(Integer.parseInt(addInfo[0]));
            try {
                lessonService.cancelLesson(lesson);
                responses.put(trainerChatId, "lesson " + lesson.getStartTime().format(Constant.formatterTimeFirst) + " was canceled");
                if (lesson.getStudent().getMessenger() == Messenger.TELEGRAM) {
                    responses.put(lesson.getStudent().getChatId(), "lesson " + lesson.getStartTime().format(Constant.formatterTimeFirst) + " was canceled");
                }
            } catch (NotFoundException e) {
                responses.put(Constant.adminChatId, e.getMessage());
            }
        } else {
            throw new CommandNotRecognizedException();
        }
        return responses;
    }


    private boolean lessonExists(LocalDate date, User student, LocalTime time) {
        if (lessonService.existByStudentDate(date, student.getId())) {
            Lesson lesson = lessonService.findByStudentDate(date, student.getId());
            if (!lesson.getStartTime().isEqual(LocalDateTime.of(date, time)) && lesson.getStatus() != LessonStatus.CANCELED) {
                throw new RuntimeException("Student " + student.getName() + " already has lesson on " + lesson.getStartTime().format(Constant.formatterTimeFirst)
                        + " can't add new lesson on time " + time.format(Constant.formatterTime));
            } else if (!lesson.getStartTime().isEqual(LocalDateTime.of(date, time)) && lesson.getStatus() == LessonStatus.CANCELED) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    @Transactional
    public HashMap<Long, String> paymentReceived(Long trainerChatId, String[] addInfo) { //payment:studentId-sum
        HashMap<Long, String> responses = new HashMap<>();
        User trainer = userService.findByChatId(trainerChatId);
        if (addInfo != null && addInfo.length == 2) {
            User student = userService.findById(Integer.parseInt(addInfo[0]));
            log.info("student found id=" + student.getId());
            int sum = Integer.parseInt(addInfo[1]);
            Payment newPayment = paymentService.saveNew(student, trainer.getId(), sum, LocalDate.now());
            log.info("payment saved id=" + newPayment.getId());
            int balance = student.getBalance();
            balance += sum;
            student.setBalance(balance);
            log.info("new balance=" + student.getBalance());
            userService.saveUser(student);
            String paymentInfo = lessonService.paymentToLessons(newPayment.getSum(), student.getId(), trainer.getId());
            responses.put(trainerChatId, "New payment from " + student.getName() + " (" + student.getId() + ") "
                    + newPayment.getSum() + " EUR received. " + paymentInfo);
            if (student.getMessenger() == Messenger.TELEGRAM) {
                responses.put(student.getChatId(), "Payment " + newPayment.getSum() + " EUR received.");
            }
        } else {
            throw new CommandNotRecognizedException();
        }
        return responses;
    }

    public String showBalance(Long trainerChatId) {
        User trainer = userService.findByChatId(trainerChatId);

        List<User> students = userService.findAllByTrainerId(trainer.getId());
        students = students.stream()
                .filter(student -> student.getBalance() != 0)
                .toList();
        int sum = 0;
        for (User student : students) {
            sum += student.getBalance();
        }
        String response = students.stream().map(UserShortDAO::new)
                .map(UserShortDAO::stringBalance)
                .collect(Collectors.joining("\n")) + "\n \n Total:" + sum;
        return response;

    }


    public String unpaidLessons(String[] addInfo, Long trainerChatId) {
        User trainer = userService.findByChatId(trainerChatId);
        String answer;
        if (addInfo == null) {
            answer = lessonService.findByStatusAndTrainerId(LessonStatus.COMPLETED, trainer.getId())
                    .stream()
                    .sorted()
                    .map(LessonAdminDAO::new)
                    .map(LessonAdminDAO::toString)
                    .collect(Collectors.joining("\n"));
        } else if (addInfo.length == 1) {
            List<Lesson> lessons = lessonService.findByStatusAndStudentId(LessonStatus.COMPLETED, Integer.parseInt(addInfo[0]));
            answer = lessons
                    .stream()
                    .sorted()
                    .map(LessonAdminDAO::new)
                    .map(LessonAdminDAO::toString)
                    .collect(Collectors.joining("\n"));
            int sumUnpaid = 0;
            for (Lesson lesson : lessons) {
                sumUnpaid += lesson.getForPayment();
            }
            answer = answer + "\ntotal: " + sumUnpaid;
        } else {
            answer = Constant.CNR;
        }
        return answer;
    }

    public HashMap<Long, String> lessonCompleted(Long trainerChatId, String[] addInfo) {
        HashMap<Long, String> responses = new HashMap<>();
        if (addInfo != null && addInfo.length == 1) {//только номер урока
            try {
                Lesson lesson = lessonService.findById(Integer.parseInt(addInfo[0]));
                if (lesson.getStatus() != LessonStatus.COMPLETED
                        && lesson.getStatus() != LessonStatus.CANCELED
                        && lesson.getStatus() != LessonStatus.PAID) {

                    lessonService.lessonCompleted(lesson);

                    responses.put(trainerChatId, "Lesson completed: \n -" + lesson.getStudent().getChatName() + " - " + lesson.getStartTime().format(Constant.formatterJustDate));
                    if (lesson.getStudent().getMessenger() == Messenger.TELEGRAM) {
                        responses.put(lesson.getStudent().getChatId(), "Lesson " + lesson.getStartTime().format(Constant.formatterJustDate) + " completed (" + lesson.getId() + ")");
                    }
                }
            } catch (RuntimeException e) {
                responses.put(trainerChatId, e.getMessage());
                responses.put(Constant.adminChatId, e.getMessage() + "log.info: lesson_completed " + addInfo[0]);
            }
        } else {
            throw new CommandNotRecognizedException();
        }
        return responses;
    }

    public String studentList(String[] addInfo, Long trainerChatId) {
        User trainer = userService.findByChatId(trainerChatId);
        if (addInfo != null && addInfo.length == 1) {
            UserRole role = UserRole.valueOf(addInfo[0].toUpperCase());
            return userService.findAllByRoleAndTrainerId(role, trainer.getId()).stream()
                    .map(UserShortDAO::new)
                    .map(UserShortDAO::toString)
                    .collect(Collectors.joining("\n"));
        } else if (addInfo == null) {
            return userService.findAllByTrainerId(trainer.getId()).stream()
                    .map(UserShortDAO::new)
                    .map(UserShortDAO::toString)
                    .collect(Collectors.joining("\n"));
        } else {
            throw new CommandNotRecognizedException();
        }
    }

    public String schedule(Long trainerChatId) {
        LocalDate now = LocalDate.now();
        User trainer = userService.findByChatId(trainerChatId);
        return
                lessonService.findInPeriodNotCanceled(now, now.plusDays(10), trainer.getId()).stream()
                        .sorted(Comparator.comparing(Lesson::getStartTime))
                        .map(LessonAdminDAO::new)
                        .map(LessonAdminDAO::toString)
                        .collect(Collectors.joining("\n"));
    }

}
