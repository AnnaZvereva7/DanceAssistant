package com.example.ZverevaDanceWCS.service.telegramBot;

import com.example.ZverevaDanceWCS.service.calendar.GoogleCalendarService;
import com.example.ZverevaDanceWCS.service.Constant;
import com.example.ZverevaDanceWCS.service.model.exception.CommandNotRecognizedException;
import com.example.ZverevaDanceWCS.service.model.lessons.LessonService;
import com.example.ZverevaDanceWCS.service.model.lessons.lessonDTO.LessonAdminDAO;
import com.example.ZverevaDanceWCS.service.model.lessons.LessonStatus;
import com.example.ZverevaDanceWCS.service.model.lessons.Lesson;
import com.example.ZverevaDanceWCS.service.model.payments.Payment;
import com.example.ZverevaDanceWCS.service.model.payments.PaymentService;
import com.example.ZverevaDanceWCS.service.model.studentInfo.InfoService;
import com.example.ZverevaDanceWCS.service.model.studentInfo.InfoStatus;
import com.example.ZverevaDanceWCS.service.model.studentInfo.StudentInfo;
import com.example.ZverevaDanceWCS.service.model.user.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TelegramBotAdmin {


    final UserService userService;

    final LessonService lessonService;

    final PaymentService paymentService;

    final GoogleCalendarService calendarService;

    final InfoService infoService;

    @Autowired
    public TelegramBotAdmin(UserService userService, LessonService lessonService, PaymentService paymentService,
                            GoogleCalendarService calendarService, InfoService infoService) {
        this.userService = userService;
        this.lessonService = lessonService;
        this.paymentService = paymentService;
        this.calendarService = calendarService;
        this.infoService = infoService;
    }


    public String printMenuAdmin() {
        StringBuilder menu = new StringBuilder("You can use one of these commands: \n");
        menu.append("/new_student:name-chat_name-messenger-role \n");
        menu.append("/new_lesson:id-01.01.25 09:30 - add new lesson \n");
        menu.append("/student_list:[role] - (id, name, chat name, status) \n");
        menu.append("/lesson_completed:01.01.25-[id] - change lesson status+add cost \n");
        menu.append("/lesson_list:startDate-[endDate] - to see list of lessons \n"); //todo не используется
        menu.append("/unpaid:[id]\n"); //список неоплаченных занятий студента
        menu.append("/change_lesson:idLesson-02.01.25 09:30\n");
        menu.append("/planned_lesson\n");
        menu.append("/add_by_schedule\n");
        menu.append("/scheduled_students\n");
        menu.append("/student_change:role-id-newRole\n");
        //menu.append("/student_change:language-id-newLanguage\n");//todo
        menu.append("/student_change:name-id-newName\n");
        menu.append("/student_change:plans-id-newPlans\n");
        //menu.append("/student_change:birthday-id-newBirthday\n");//todo
        menu.append("/show_plans:[id]\n");
        //menu.append("student_info\n");//todo вывести информацию о студенте с планами и последними рекапами
        menu.append("/payment:id-sum\n");
        menu.append("/balance:[id]\n");
        menu.append("/add_recap:lessonId-recap\n");//todo убрать
        menu.append("/add_info:studentId-info\n");
        menu.append("/change_schedule:id-[day-09:30]\n");
        menu.append("/cancel_lesson:lessonId\n");
        menu.append("/send_bill:studentId\n");
        menu.append("/change_duration:lessonId-durationMin\n");
        menu.append("/month_payments\n");
        menu.append("/year_payments\n");
        menu.append("show_info:studentId\n");

        //"existed_to_google" для разового использования

        return menu.toString();
    }

    //month_payments:2025-1 - оплаты за месяц
    public String monthPayment(String[] addInfo) {
        if (addInfo != null && addInfo.length == 2) {
            int year = Integer.parseInt(addInfo[0]);
            int month = Integer.parseInt(addInfo[1]);
            List<Payment> payments = paymentService.findByMonthAndYear(month, year);
            int sum = 0;
            for (Payment payment : payments) {
                sum += payment.getSum();
            }
            return String.valueOf(sum);
        } else {
            throw new CommandNotRecognizedException(Constant.CNR);
        }
    }

    //year_payments:2025
    public String yearPayments(String[] addInfo) {
        if (addInfo != null && addInfo.length == 1) {
            int year = Integer.parseInt(addInfo[0]);
            int sum = paymentService.findByYear(year).stream().mapToInt(Payment::getSum).sum();
            return String.valueOf(sum);
        } else {
            throw new CommandNotRecognizedException(Constant.CNR);
        }
    }

    //add_info:studentId-info
    public String addInfo(String[] addInfo) {
        if (addInfo != null && addInfo.length == 2) {
            StudentInfo info = new StudentInfo();
            info.setStudentId(Integer.parseInt(addInfo[0]));
            info.setInfo(addInfo[1]);
            info.setDate(LocalDate.now());
            info.setStatus(InfoStatus.ACTUAL);
            return "New information for " + userService.findById(Integer.parseInt(addInfo[0])).getName() + ": " + infoService.save(info).toString();
        } else {
            throw new CommandNotRecognizedException(Constant.CNR);
        }
    }

    //show_info:studentId
    //show_info (for every actual info)
    public String showInfo(String[] addInfo) {
        if (addInfo != null && addInfo.length == 1) {
            User student = userService.findById(Integer.parseInt(addInfo[0]));
            return student.getName() + " info:\n"
                    + infoService.findByStudentActual(Integer.parseInt(addInfo[0]));
        } else if (addInfo == null) {
            Map<Integer, String> infoMap = infoService.findAllByStatus(InfoStatus.ACTUAL);
            String response = "";
            for (Map.Entry<Integer, String> entry : infoMap.entrySet()) {
                User student = userService.findById(entry.getKey());
                response = response + "\n\n" + student.getName() + " info:\n" + entry.getValue();
            }
            return response;
        } else {
            throw new CommandNotRecognizedException(Constant.CNR);
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
            throw new CommandNotRecognizedException(Constant.CNR);
        }
    }

    //to transfer recap to info
    public String fromRecapToInfo() {
        int amount = 0;
        List<Lesson> lessons = lessonService.findByRecapIsNotNull();
        for (Lesson lesson : lessons) {
            StudentInfo info = new StudentInfo();
            info.setStudentId(lesson.getStudent().getId());
            info.setStatus(InfoStatus.ACTUAL);
            info.setInfo(lesson.getRecap());
            info.setDate(lesson.getStartTime().toLocalDate());
            infoService.save(info);
            amount += 1;
        }
        return "Added " + amount + " new infos";
    }

    //change_schedule:id-[day-09:30]
    public HashMap<Long, String> changeSchedule(String[] addInfo) { //student_change_schedule:studentId-[day-time]
        HashMap<Long, String> response = new HashMap<>();
        if (addInfo != null && addInfo.length == 1) {//delete schedule
            User student = userService.findById(Integer.parseInt(addInfo[0]));
            student.setScheduleDay(null);
            student.setScheduleTime(null);
            userService.saveUser(student);
            List<Lesson> lessons = lessonService.findByStudentAndDateAfter(student.getId(), LocalDateTime.now());
            String listFutureLessons = lessons.stream().sorted().map(LessonAdminDAO::new).map(LessonAdminDAO::toString).collect(Collectors.joining("\n"));
            response.put(Constant.adminChatId, "Удалено распичание у " + student.getName() + " оставшиеся уроки: \n" + listFutureLessons);
        } else if (addInfo != null && addInfo.length == 3) {//изменение в расписание на новое, если ранее не было расписания то перейти в блок добавить расписание
            User student = userService.findById(Integer.parseInt(addInfo[0]));
            DayOfWeek day = DayOfWeek.valueOf(addInfo[1]);
            LocalTime time = LocalTime.parse(addInfo[2], Constant.timeFormatter);
            if (student.getScheduleDay() != null) {//расписание есть и мы его только меняем, выводя список уроков, которые уже есть по расписанию старому
                student.setScheduleDay(day);
                student.setScheduleTime(time);
                userService.saveUser(student);
                String listFutureLessons = lessonService.findByStudentAndDateAfter(student.getId(), LocalDateTime.now())
                        .stream()
                        .sorted()
                        .map(LessonAdminDAO::new)
                        .map(LessonAdminDAO::toString)
                        .collect(Collectors.joining("\n"));
                response.put(Constant.adminChatId, "Изменено расписание для " + student.getName() + " :"
                        + student.getScheduleDay() + " at " + student.getScheduleTime().format(Constant.timeFormatter)
                        + " \n В расписании стоят следующие уроки: \n" + listFutureLessons);
                if (student.getMessenger() == Messenger.TELEGRAM) {
                    response.put(student.getChatId(), "Изменено расписание: " + student.getScheduleDay() + " at "
                            + student.getScheduleTime().format(Constant.timeFormatter));
                }
            } else { //расписания нет мы его добавляем и сразу добавляем уроки на 3 недели
                student.setScheduleDay(day);
                student.setScheduleTime(time);
                userService.saveUser(student);
                LocalDate date = LocalDate.now().with(TemporalAdjusters.nextOrSame(day));
                for (int i = 0; i < 3; i++) {
                    Boolean lessonExists = lessonExists(date.plusWeeks(i), student, time);
                    if (!lessonExists) {
                        try {
                            Lesson lesson = lessonService.saveNewLesson(new Lesson(student, LocalDateTime.of(date, time).plusWeeks(i), LessonStatus.PLANNED));
                        } catch (RuntimeException e) {
                            response.put(Constant.adminChatId, e.getMessage());
                        }
                    }
                }
                String listFutureLessons = lessonService.findByStudentAndDateAfter(student.getId(), LocalDateTime.now())
                        .stream()
                        .sorted()
                        .map(LessonAdminDAO::new)
                        .map(LessonAdminDAO::toString)
                        .collect(Collectors.joining("\n"));
                response.put(Constant.adminChatId, "Добавлено расписание для " + student.getName() + " :"
                        + student.getScheduleDay() + " at " + student.getScheduleTime().format(Constant.timeFormatter)
                        + " \n В расписании стоят следующие уроки: \n" + listFutureLessons);
                if (student.getMessenger() == Messenger.TELEGRAM) {
                    response.put(student.getChatId(), "Добавлено расписание: " + student.getScheduleDay() + " at "
                            + student.getScheduleTime().format(Constant.timeFormatter) + "\n В расписании стоят следующие уроки: \n" + listFutureLessons);
                }
            }
        } else {
            throw new CommandNotRecognizedException(Constant.CNR);
        }

        return response;
    }

    //add_by_schedule - добавление уроков по расписанию на ближайшую неделю
    public HashMap<Long, String> addBySchedule() {
        HashMap<Long, String> response = new HashMap<>();
        List<User> users = userService.usersWithSchedule();
        String lessonInfoAdmin = "New lessons added: /n";
        for (User user : users) {
            Lesson lesson = lessonService.addLessonBySchedule(user);
            String lessonInfo = "New lesson added: " + lesson.getStartTime().format(Constant.formatter);
            if (user.getMessenger() == Messenger.TELEGRAM) {
                response.put(user.getChatId(), lessonInfo);
            }
            lessonInfoAdmin = lessonInfoAdmin
                    + lessonInfo + " " + user.getName() + " (lessonId=" + lesson.getId() + ")\n";
        }
        response.put(Constant.adminChatId, lessonInfoAdmin);
        return response;
    }

    //planned_lesson
    public String plannedLessons() {
        String answer = "";
        List<Lesson> lessons = lessonService.findByStatus(LessonStatus.PLANNED);
        for (Lesson lesson : lessons) {
            answer = answer + lesson.getStartTime().format(Constant.formatter) + " - "
                    + lesson.getStudent().getName() + " - "
                    + lesson.getStatus() + " ("
                    + lesson.getId() + ");\n";
        }
        return answer;
    }

    //scheduled_students - список учеников с расписанием и их расписание
    public String scheduledStudents() {
        String answer = "";
        List<User> users = userService.usersWithSchedule();
        users.sort(User.compareBySchedule());
        for (User user : users) {
            answer = answer + user.getScheduleDay() + " "
                    + user.getScheduleTime().format(Constant.timeFormatter) + " - "
                    + user.getName() + " (" + user.getId() + ");\n";
        }
        return answer;
    }

    //todo чтоб расчет стоимости правильно велся для уроков запланированных
    public String changeDuration(String[] addInfo) {
        if (addInfo != null && addInfo.length == 2) {
            String answer = "";
            Lesson lesson = lessonService.findById(Integer.parseInt(addInfo[0]));
            if (lesson.getStatus() == LessonStatus.COMPLETED) {
                int durationOld = lesson.getDurationMin();
                lesson.changeDuration(Integer.parseInt(addInfo[1]));
                int costOld = lesson.getCost();
                lesson.setCost(costOld* lesson.getDurationMin()/durationOld);
                int forPaymentOld = lesson.getForPayment();
                if(costOld-forPaymentOld>0) {
                    lesson.setForPayment(lesson.getCost()-(costOld - forPaymentOld));
                } else {
                    lesson.setForPayment(lesson.getCost());
                }
                lessonService.updateLessonInBase(lesson);
            } else if (lesson.getStatus() == LessonStatus.NEW || lesson.getStatus() == LessonStatus.PLANNED) {
                lesson.changeDuration(Integer.parseInt(addInfo[1]));
                lessonService.updateLesson(lesson);
            } else {
                throw new RuntimeException("Lesson has status " + lesson.getStatus() + ". Duration can't be changed.");
            }
            answer = answer + "New duration " + lesson.getDurationMin() + ", new cost= " + lesson.getCost() + " EUR.";
            return answer;
        } else {
            throw new CommandNotRecognizedException(Constant.CNR);
        }
    }

    public HashMap<Long, String> sendBill(String[] addInfo) { //send_bill:studentId
        HashMap<Long, String> response = new HashMap<>();
        if (addInfo != null && addInfo.length == 1) {
            User student = userService.findById(Integer.parseInt(addInfo[0]));
            List<Lesson> lessons = lessonService.findByStudentIdAndStatus(student.getId(), LessonStatus.COMPLETED).stream()
                    .sorted()
                    .collect(Collectors.toList());
            String answer = lessonService.lessonsToBill(lessons);
            if (student.getMessenger() == Messenger.TELEGRAM) {
                response.put(student.getChatId(), answer);
            }
            response.put(Constant.adminChatId, student.getName() + "\n" + answer);
            return response;
        } else {
            throw new CommandNotRecognizedException(Constant.CNR);
        }
    }

    public void existedToGoogle() {
        lessonService.addExistedToGoogle();
    }

    public HashMap<Long, String> cancelLesson(String[] addInfo) {
        HashMap<Long, String> response = new HashMap<>();
        if (addInfo != null && addInfo.length == 1) {
            Lesson lesson = lessonService.findById(Integer.parseInt(addInfo[0]));
            try {
                lessonService.cancelLesson(lesson);
                response.put(Constant.adminChatId, "lesson " + lesson.getStartTime().format(Constant.formatterTimeFirst) + " was canceled");
                if (lesson.getStudent().getMessenger() == Messenger.TELEGRAM) {
                    response.put(lesson.getStudent().getChatId(), "lesson " + lesson.getStartTime().format(Constant.formatterTimeFirst) + " was canceled");
                }
            } catch (RuntimeException e) {
                response.put(Constant.adminChatId, e.getMessage());
            }
        } else {
            throw new CommandNotRecognizedException(Constant.CNR);
        }
        return response;
    }


    public boolean lessonExists(LocalDate date, User student, LocalTime time) {
        if (lessonService.existByStudentDate(date, student.getId())) {
            Lesson lesson = lessonService.findByStudentDate(date, student.getId());
            if (!lesson.getStartTime().isEqual(LocalDateTime.of(date, time)) && lesson.getStatus() != LessonStatus.CANCELED) {
                throw new RuntimeException("Student " + student.getName() + " already has lesson on " + lesson.getStartTime().format(Constant.formatterTimeFirst)
                        + " can't add new lesson on time " + time.format(Constant.timeFormatter));
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
    public HashMap<Long, String> paymentReceived(String[] addInfo) { //payment:studentId-sum
        HashMap<Long, String> response = new HashMap<>();
        if (addInfo != null && addInfo.length == 2) {
            User student = userService.findById(Integer.parseInt(addInfo[0]));
            log.info("student found id=" + student.getId());
            int sum = Integer.parseInt(addInfo[1]);
            Payment newPayment = paymentService.saveNew(student, sum, LocalDate.now());
            log.info("payment saved id=" + newPayment.getId());
            int balance = student.getBalance();
            balance += sum;
            student.setBalance(balance);
            log.info("new balance=" + student.getBalance());
            userService.saveUser(student);
            String paymentInfo = lessonService.paymentToLessons(student.getId(), newPayment.getSum());
            response.put(Constant.adminChatId, "New payment from " + student.getName() + " (" + student.getId() + ") "
                    + newPayment.getSum() + " EUR received. " + paymentInfo);
            if (student.getMessenger() == Messenger.TELEGRAM) {
                response.put(student.getChatId(), "Payment " + newPayment.getSum() + " EUR received.");
            }
            return response;
        } else if (addInfo != null && addInfo.length == 1) {
            User student = userService.findById(Integer.parseInt(addInfo[0]));
            List<Payment> payments = paymentService.findByStudent(student.getId());
            String answer = payments.stream()
                    .map(Payment::toString)
                    .collect(Collectors.joining("\n"));
            response.put(Constant.adminChatId, answer);
            return response;
        } else {
            throw new CommandNotRecognizedException(Constant.CNR);
        }
    }

    public HashMap<Long, String> recap(String[] addInfo) { //add_recap:lessonId-recap
        HashMap<Long, String> response = new HashMap<>();
        if (addInfo != null && addInfo.length == 2) {
            int lessonId = Integer.parseInt(addInfo[0]);
            Lesson lesson = lessonService.findById(lessonId);
            lesson.setRecap(addInfo[1]);
            lesson = lessonService.updateLessonInBase(lesson);
            User student = lesson.getStudent();
            if (student.getMessenger() == Messenger.TELEGRAM) {
                response.put(student.getChatId(), "New recap " + lesson.getStartTime().format(Constant.formatterJustDate) + ": " + lesson.getRecap());
            }
            response.put(Constant.adminChatId, "Recap added");
        } else {
            throw new CommandNotRecognizedException(Constant.CNR);
        }
        return response;
    }

    public String showBalance(String[] addInfo) {
        if (addInfo == null) {
            List<User> students = userService.findAll();
            students = students.stream()
                    .filter(student -> student.getBalance() != 0)
                    .sorted(User.compareById())
                    .toList();
            int sum = 0;
            for (User student : students) {
                sum += student.getBalance();
            }
            return students.stream().map(UserShortDAO::new)
                    .map(UserShortDAO::stringBalance)
                    .collect(Collectors.joining("\n")) + "\n \n Total:" + sum;
        } else if (addInfo.length == 1) {
            User student = userService.findById(Integer.parseInt(addInfo[0]));
            UserShortDAO userDao = new UserShortDAO(student);
            return userDao.stringBalance();
        } else {
            throw new CommandNotRecognizedException(Constant.CNR);
        }
    }

    public String showPlans(String[] addInfo) {
        if (addInfo == null) {
            List<User> students = userService.findAll();
            return students.stream()
                    .filter(student -> student.getPlans() != null)
                    .map(UserShortDAO::new)
                    .map(UserShortDAO::stringPlans)
                    .collect(Collectors.joining("\n"));
        } else if (addInfo.length == 1) {
            User student = userService.findById(Integer.parseInt(addInfo[0]));
            UserShortDAO userDao = new UserShortDAO(student);
            return userDao.stringPlans();
        } else {
            throw new CommandNotRecognizedException(Constant.CNR);
        }
    }

    public String changeStudent(String[] addInfo) {
        if (addInfo != null && addInfo.length == 3) {
            String field = addInfo[0];
            User student = userService.findById(Integer.parseInt(addInfo[1]));
            switch (field) {
                case "role":
                    student.setRole(UserRole.valueOf(addInfo[2]));
                    student = userService.saveUser(student);
                    return "New role for " + student.getName() + ": " + student.getRole();
                case "language":
                    //todo добавить изменение языка
                    return "";
                case "name":
                    student.setName(addInfo[2]);
                    student = userService.saveUser(student);
                    return "New name for " + student.getChatName() + ": " + student.getName();
                case "birthday":
                    //todo изменить день рождения
                    return "";
                default:
                    throw new CommandNotRecognizedException("Wrong name of field to change");
            }

        } else {
            throw new CommandNotRecognizedException(Constant.CNR);
        }
    }


    public HashMap<Long, String> changeLessonTime(String[] addInfo) { //change_lesson:idLesson-02.01.25 09:30
        if (addInfo != null && addInfo.length == 2) {
            int lessonId = Integer.parseInt(addInfo[0]);
            Lesson lesson = lessonService.findById(lessonId);

            LocalDateTime oldDate = lesson.getStartTime();

            LocalDateTime newDateTime = LocalDateTime.parse(addInfo[1], Constant.formatter);

            if (lesson.getStatus() == LessonStatus.NEW || lesson.getStatus() == LessonStatus.PLANNED) {
                lesson.setStartTime(newDateTime);
                lesson.setStatus(LessonStatus.PLANNED);
                lessonService.updateLesson(lesson);
            }
            HashMap<Long, String> answers = new HashMap<>();
            answers.put(Constant.adminChatId, "Перенесен урок с " + oldDate.format(Constant.formatterJustDate) +
                    " на " + newDateTime.format(Constant.formatterTimeFirst) + " у " + lesson.getStudent().getName());
            if (lesson.getStudent().getMessenger() == Messenger.TELEGRAM) {
                answers.put(lesson.getStudent().getChatId(), "Date of lesson " + oldDate.format(Constant.formatterJustDate) +
                        " was changed. New date and time " + newDateTime.format(Constant.formatterTimeFirst));
            }
            return answers;
        } else {
            throw new CommandNotRecognizedException(Constant.CNR);
        }

    }

    public String unpaidLessons(String[] addInfo) {
        String answer;
        if (addInfo == null) {
            answer = lessonService.findByStatus(LessonStatus.COMPLETED)
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
                sumUnpaid += lesson.getForPayment();//todo change forPayment
            }
            answer = answer + "\ntotal: " + sumUnpaid;
        } else {
            answer = Constant.CNR;
        }
        return answer;
    }

    public String lessonList(String[] addInfo) {
        String answer;
        if (addInfo != null && addInfo.length == 1) { // только одна дата (вторая дата - now, может быть промежуток в прошлом или будущем)
            LocalDate date = LocalDate.parse(addInfo[0], Constant.formatterJustDate);
            if (date.isBefore(LocalDate.now())) {
                answer = lessonService.findInPeriod(date, LocalDate.now()).stream()
                        .sorted()
                        .map(LessonAdminDAO::new)
                        .map(LessonAdminDAO::toString)
                        .collect(Collectors.joining("\n"));
            } else {
                answer = lessonService.findInPeriod(LocalDate.now(), date).stream()
                        .sorted()
                        .map(LessonAdminDAO::new)
                        .map(LessonAdminDAO::toString)
                        .collect(Collectors.joining("\n"));
            }

        } else if (addInfo != null && addInfo.length == 2) {// две даты начала и конца временного промежутка
            answer = lessonService.findInPeriod(LocalDate.parse(addInfo[0], Constant.formatterJustDate),
                            LocalDate.parse(addInfo[1], Constant.formatterJustDate))
                    .stream()
                    .sorted()
                    .map(LessonAdminDAO::new)
                    .map(LessonAdminDAO::toString)
                    .collect(Collectors.joining("\n"));
        } else {
            answer = Constant.CNR;
        }
        return answer;
    }

    public HashMap<Long, String> lessonCompleted(String[] addInfo) {
        HashMap<Long, String> answers = new HashMap<>();
        if (addInfo != null && addInfo.length == 1) {//только дата или только номер урока
            LocalDate date;
            try {
                date = LocalDate.parse(addInfo[0], Constant.formatterJustDate);
                List<Lesson> lessons = lessonService.findByDate(date).stream()
                        .sorted()
                        .collect(Collectors.toList());
                StringBuilder answerAdmin = new StringBuilder("Lessons completed: \n");
                for (Lesson lesson : lessons) {
                    if (lesson.getStatus() == LessonStatus.NEW || lesson.getStatus() == LessonStatus.PLANNED) {
                        User student = lesson.getStudent();
                        if (student.getMessenger() == Messenger.TELEGRAM) {
                            answers.put(student.getChatId(), "Lesson " + lesson.getStartTime().format(Constant.formatterJustDate) + " COMPLETED (" + lesson.getId() + ")");
                        }
                        lessonService.lessonCompleted(lesson);

                        answerAdmin.append("- ")
                                .append(lesson.getStudent().getChatName())
                                .append(" - ")
                                .append(lesson.getStartTime().format(Constant.formatterJustDate))
                                .append("- COMPLETED (")
                                .append(lesson.getId())
                                .append(")\n");
                    }
                    if (lesson.getStatus() == LessonStatus.PAID || lesson.getStatus() == LessonStatus.CANCELED) {
                        answerAdmin.append("!!! Status:")
                                .append(lesson.getStatus().toString())
                                .append(" - student:")
                                .append(lesson.getStudent().getName())
                                .append(" date: ")
                                .append(lesson.getStartTime().format(Constant.formatterJustDate))
                                .append("\n");
                    }
                }
                answers.put(Constant.adminChatId, answerAdmin.toString());
            } catch (DateTimeParseException e) {
                Lesson lesson = lessonService.findById(Integer.parseInt(addInfo[0]));
                if (lesson.getStatus() != LessonStatus.COMPLETED
                        && lesson.getStatus() != LessonStatus.CANCELED
                        && lesson.getStatus() != LessonStatus.PAID) {

                    lessonService.lessonCompleted(lesson);

                    answers.put(Constant.adminChatId, "Lesson completed: \n -" + lesson.getStudent().getChatName() + " - " + lesson.getStartTime().format(Constant.formatterJustDate));
                    if (lesson.getStudent().getMessenger() == Messenger.TELEGRAM) {
                        answers.put(lesson.getStudent().getChatId(), "Lesson " + lesson.getStartTime().format(Constant.formatterJustDate) + " completed (" + lesson.getId() + ")");
                    }
                }
            } catch (RuntimeException e) {
                answers.put(Constant.adminChatId, e.getMessage());
            }
        } else {
            throw new CommandNotRecognizedException(Constant.CNR);
        }
        return answers;
    }

    public String studentList(String[] addInfo) {
        UserRole role = null;
        if (addInfo != null && addInfo.length > 1) {
            log.info("Wrong command, with additional information");
            return "Wrong command!";
        } else if (addInfo != null && addInfo.length == 1) {
            role = UserRole.valueOf(addInfo[0].toUpperCase());
        }
        return userService.findAllByRole(role).stream()
                .map(UserShortDAO::new)
                .map(UserShortDAO::toString)
                .collect(Collectors.joining("\n"));
    }

    public String newLesson(String[] addInfo) { //new_lesson:id-01.01.25 09:30
        if (addInfo != null && addInfo.length == 2) {
            LocalDateTime dateTime = LocalDateTime.parse(addInfo[1], Constant.formatter);
            User student = userService.findById(Integer.parseInt(addInfo[0]));
            Lesson newLesson = new Lesson(student, dateTime, LessonStatus.PLANNED);
            newLesson = lessonService.saveNewLesson(newLesson);
            return "New lesson added " + Constant.formatter.format(newLesson.getStartTime()) + " (id=" + newLesson.getId() + ")";
        } else if (addInfo != null && addInfo.length == 4) { //new_lesson:id-18.11.25 19:00-3-50
            User student = userService.findById(Integer.parseInt(addInfo[0]));
            LocalDateTime dateTime = LocalDateTime.parse(addInfo[1], Constant.formatter);
            Lesson newLesson = new Lesson(student, dateTime, Integer.parseInt(addInfo[2]), Integer.parseInt(addInfo[3]));
            newLesson = lessonService.saveNewLesson(newLesson);
            return "New lesson added " + Constant.formatter.format(newLesson.getStartTime()) + " (id=" + newLesson.getId() + ")";
        } else {
            log.info(Constant.CNR);
            return Constant.CNR;
        }
    }

    public String newStudent(String[] addInfo) {
        if (addInfo != null && addInfo.length == 4) {
            User newUser = new User(addInfo[0], addInfo[1], Messenger.valueOf(addInfo[2].toUpperCase()), UserRole.valueOf(addInfo[3].toUpperCase()));
            User returnedUser = userService.saveUser(newUser);
            return "New user added: id=" + returnedUser.getId() + " name=" + returnedUser.getName() + " chatName=" + returnedUser.getChatName() + ".";
        } else {
            return Constant.CNR;
        }
    }

    public String schedule() {
        LocalDate now = LocalDate.now();
        return lessonService.findInPeriodNotCanceled(now, now.plusDays(10)).stream()
                .sorted(Comparator.comparing(Lesson::getStartTime))
                .map(LessonAdminDAO::new)
                .map(LessonAdminDAO::toString)
                .collect(Collectors.joining("\n"));

    }

    //TODO проверка даты и даты со временем, происходит только по дате, без учета времени
    private boolean checkDate(LocalDateTime dateTime, int deltaInDays) {
        return dateTime.isBefore(LocalDateTime.now().plusDays(deltaInDays));
    }

}
