package com.example.ZverevaDanceWCS.service.telegramBot;

import com.example.ZverevaDanceWCS.service.calendar.GoogleCalendarService;
import com.example.ZverevaDanceWCS.service.Constant;
import com.example.ZverevaDanceWCS.service.model.exception.CommandNotRecognizedException;
import com.example.ZverevaDanceWCS.service.model.exception.WrongDateException;
import com.example.ZverevaDanceWCS.service.model.lessons.LessonService;
import com.example.ZverevaDanceWCS.service.model.lessons.LessonAdminDAO;
import com.example.ZverevaDanceWCS.service.model.lessons.LessonStatus;
import com.example.ZverevaDanceWCS.service.model.lessons.Lesson;
import com.example.ZverevaDanceWCS.service.model.payments.Payment;
import com.example.ZverevaDanceWCS.service.model.payments.PaymentService;
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
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TelegramBotAdmin {


    final UserService userService;

    final LessonService lessonService;

    final PaymentService paymentService;

    final GoogleCalendarService calendarService;

    @Autowired
    public TelegramBotAdmin(UserService userService, LessonService lessonService, PaymentService paymentService, GoogleCalendarService calendarService) {
        this.userService = userService;
        this.lessonService = lessonService;
        this.paymentService = paymentService;
        this.calendarService = calendarService;
    }


    public String printMenuAdmin() {
        StringBuilder menu = new StringBuilder("You can use one of these commands: \n");
        menu.append("/new_student:name-chat_name-messenger-role \n");
        menu.append("/new_lesson:id-01.01.25 09:30 - add new lesson \n");
        menu.append("/student_list:[role] - (id, name, chat name) \n");
        menu.append("/lesson_completed:01.01.25-[id] - change lesson status+add cost \n");
        menu.append("/lesson_list:startDate-[endDate] - to see list of lessons \n");
        menu.append("/unpaid:[name]\n");
        menu.append("/change_lesson:id-01.01.25-02.01.25 09:30\n");

        menu.append("/student_change:role-id-newRole\n");
        //menu.append("/student_change:language-id-newLanguage\n");//todo
        menu.append("/student_change:name-id-newName\n");
        menu.append("/student_change:plans-id-newPlans\n");
        //menu.append("/student_change:birthday-id-newBirthday\n");//todo

        menu.append("/show_plans:[id]\n");
        //menu.append("/cancel_lesson:id-[01.01.25]\n"); //todo
       menu.append("/payment:id-sum\n");
        menu.append("/balance:[id]\n");
        menu.append("/add_recap:lessonId-recap\n");
        menu.append("/add_schedule:id-day-09:30\n");
        menu.append("/cancel_lesson:lessonId\n");
        menu.append("/send_bill:studentId\n");
       //"existed_to_google" для разового использования

        return menu.toString();
    }

    public HashMap<Long, String> sendBill(String[] addInfo) { //send_bill:studentId
        HashMap<Long,String> response = new HashMap<>();
        if(addInfo!=null && addInfo.length==1) {
            User student = userService.findById(Integer.parseInt(addInfo[0]));
            List<Lesson> lessons=lessonService.findByStudentIdAndStatus(student.getId(), LessonStatus.COMPLETED).stream()
                    .sorted()
                    .collect(Collectors.toList());
            String answer="Completed lessons:\n";
            int total=0;
            for(Lesson lesson:lessons) {
                answer=answer+lesson.getStartTime().format(Constant.formatterJustDate)+" - "+lesson.getForPayment()+" EUR\n";
                total+=lesson.getForPayment();
            }
            answer=answer+"To pay: "+total+" EUR";
            if(student.getMessenger()==Messenger.TELEGRAM) {
                response.put(student.getChatId(), answer);
            }
            response.put(Constant.adminChatId, student.getName()+"\n"+answer);
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
        if(addInfo!=null && addInfo.length==1) {
            Lesson lesson=lessonService.findById(Integer.parseInt(addInfo[0]));
            try {
                lessonService.cancelLesson(lesson);
                response.put(Constant.adminChatId, "lesson "+lesson.getStartTime().format(Constant.formatterTimeFirst)+ " was canceled");
                if(lesson.getStudent().getMessenger()==Messenger.TELEGRAM) {
                    response.put(lesson.getStudent().getChatId(), "lesson "+lesson.getStartTime().format(Constant.formatterTimeFirst)+ " was canceled");
                }
            } catch (RuntimeException e ) {
                response.put(Constant.adminChatId, e.getMessage());
            }
        }else {
            throw new CommandNotRecognizedException(Constant.CNR);
        }
        return response;
    }

    public String addSchedule(String[] addInfo) {
        if (addInfo != null && addInfo.length == 3) {
            User student = userService.findById(Integer.parseInt(addInfo[0]));
            String answer = "Added next new lesson for "+student.getName()+":\n";
            DayOfWeek day = DayOfWeek.valueOf(addInfo[1]);
            LocalTime time = LocalTime.parse(addInfo[2], Constant.timeFormatter);
            LocalDate date = LocalDate.now().with(TemporalAdjusters.nextOrSame(day));
            student.setScheduleDay(day);
            student.setScheduleTime(time);
            userService.saveUser(student);
            for (int i=0; i<3; i++) {
                try{
                    Boolean lessonExists = lessonExists(date.plusWeeks(i), student, time);
                    if(!lessonExists) {
                        Lesson lesson=lessonService.saveNewLesson(new Lesson(student, LocalDateTime.of(date, time).plusWeeks(i), LessonStatus.PLANNED));
                        answer=answer+lesson.getStartTime().format(Constant.formatterTimeFirst)+" ("+lesson.getId()+")\n";
                    }
                } catch (RuntimeException e) {
                    answer=answer+e.getMessage()+"\n";
                }
            }
            return answer;
        } else {
            throw new CommandNotRecognizedException(Constant.CNR);
        }
    }

    public boolean lessonExists(LocalDate date, User student, LocalTime time) {
        if (lessonService.existByStudentDate(date, student.getId())) {
            Lesson lesson = lessonService.findByStudentDate(date, student.getId());
            if (!lesson.getStartTime().isEqual(LocalDateTime.of(date, time))&&lesson.getStatus()!=LessonStatus.CANCELED) {
                throw new RuntimeException("Student " + student.getName() + " already has lesson on " + lesson.getStartTime().format(Constant.formatterTimeFirst)
                        + " can't add new lesson on time " + time.format(Constant.timeFormatter));
            } else if (!lesson.getStartTime().isEqual(LocalDateTime.of(date, time))&&lesson.getStatus()==LessonStatus.CANCELED) {
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
            response.put(Constant.adminChatId, "New payment from " + student.getName() + " " + newPayment.getSum() + " EUR received. " + paymentInfo);
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
            return students.stream()
                    .filter(student -> student.getBalance() != 0)
                    .map(UserShortDAO::new)
                    .map(UserShortDAO::stringBalance)
                    .collect(Collectors.joining("\n"));
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
                case "plans":
                    //todo check email
                    student.setPlans(addInfo[2]);
                    student = userService.saveUser(student);
                    return "New plans for " + student.getName();
                case "birthday":
                    //todo изменить день рождения
                    return "";
                case "schedule":
                    //todo убрать уроки по старому расписанию и добавить уроки по новому расписанию
                    return "";
                default:
                    throw new CommandNotRecognizedException("Wrong name of field to change");
            }

        } else {
            throw new CommandNotRecognizedException(Constant.CNR);
        }
    }


    public HashMap<Long, String> changeLessonTime(String[] addInfo) { //change_lesson:id-01.01.25-02.01.25 09:30
        if (addInfo != null && addInfo.length == 3) {
            int studentId = Integer.parseInt(addInfo[0]);
            User student = userService.findById(studentId);
            LocalDate oldDate = LocalDate.parse(addInfo[1], Constant.formatterJustDate);
            if (oldDate.isBefore(LocalDate.now())) {
                throw new WrongDateException("You can't change lesson in the past");
            }
            LocalDateTime newDateTime = LocalDateTime.parse(addInfo[2], Constant.formatter);
            if (newDateTime.isBefore(LocalDateTime.now())) {
                throw new WrongDateException("New date must be in future");
            }
            Lesson lesson = lessonService.findByStudentDate(oldDate, studentId);
            if (lesson.getStatus() == LessonStatus.NEW || lesson.getStatus() == LessonStatus.PLANNED) {
                lesson.setStartTime(newDateTime);
                lesson.setStatus(LessonStatus.PLANNED);
                lessonService.updateLesson(lesson);
            }
            HashMap<Long, String> answers = new HashMap<>();
            answers.put(Constant.adminChatId, "Перенесен урок с " + oldDate.format(Constant.formatterJustDate) +
                    " на " + newDateTime.format(Constant.formatterTimeFirst) + " у " + student.getName());
            if (student.getMessenger() == Messenger.TELEGRAM) {
                answers.put(student.getChatId(), "Date of lesson " + oldDate.format(Constant.formatterJustDate) +
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
            List<Lesson> lessons=lessonService.findByStatusAndStudentName(LessonStatus.COMPLETED, addInfo[0]);
            answer = lessons
                    .stream()
                    .sorted()
                    .map(LessonAdminDAO::new)
                    .map(LessonAdminDAO::toString)
                    .collect(Collectors.joining("\n"));
            int sumUnpaid=0;
            for (Lesson lesson:lessons) {
                sumUnpaid+=lesson.getCost();//todo change forPayment
            }
            answer = answer+"\ntotal: "+sumUnpaid;
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
                        lesson.setCost();
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
                        lessonService.updateLessonInBase(lesson);

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
                lesson.setStatus(LessonStatus.COMPLETED);
                lesson.setCost();

                User student = lesson.getStudent();
                int balance = student.getBalance();
                int forPay = lesson.getForPayment();

                student.setBalance(balance - forPay);

                if (balance >= forPay) {
                    lesson.setForPayment(0);
                    lesson.setStatus(LessonStatus.PAID);
                } else if (balance > 0) {
                    lesson.setForPayment(forPay - balance);
                    lesson.setStatus(LessonStatus.COMPLETED);
                } else {
                    lesson.setStatus(LessonStatus.COMPLETED);
                }
                userService.saveUser(student);
                lessonService.updateLessonInBase(lesson);

                answers.put(Constant.adminChatId, "Lesson completed: \n -" + lesson.getStudent().getChatName() + " - " + lesson.getStartTime().format(Constant.formatterJustDate));
                if (student.getMessenger() == Messenger.TELEGRAM) {
                    answers.put(lesson.getStudent().getChatId(), "Lesson " + lesson.getStartTime().format(Constant.formatterJustDate) + " completed (" + lesson.getId() + ")");
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
            if (checkDate(dateTime, 0)) {
                log.info("Date before now");
                return "Wrong date!";
            }
            User student = userService.findById(Integer.parseInt(addInfo[0]));
            Lesson newLesson = new Lesson(student, dateTime, LessonStatus.PLANNED);
            newLesson = lessonService.saveNewLesson(newLesson);
            return "New lesson added " + Constant.formatter.format(newLesson.getStartTime());
        } else {
            log.info(Constant.CNR);
            return Constant.CNR;
        }
    }

    public String newStudent(String[] addInfo) {
        if (addInfo != null && addInfo.length == 4) {
            User newUser = new User(addInfo[0], addInfo[1], addInfo[2], UserRole.valueOf(addInfo[3].toUpperCase()));
            User returnedUser = userService.saveUser(newUser);
            return "New user added: id=" + returnedUser.getId() + " name=" + returnedUser.getName() + " chatName=" + returnedUser.getChatName() + ".";
        } else {
            return Constant.CNR;
        }
    }

    public String schedule() {
        LocalDate now = LocalDate.now();
        return lessonService.findInPeriod(now, now.plusDays(10)).stream()
                .sorted()
                .map(LessonAdminDAO::new)
                .map(LessonAdminDAO::toString)
                .collect(Collectors.joining("\n"));

    }

    //TODO проверка даты и даты со временем, происходит только по дате, без учета времени
    private boolean checkDate(LocalDateTime dateTime, int deltaInDays) {
        return dateTime.isBefore(LocalDateTime.now().plusDays(deltaInDays));
    }

}
