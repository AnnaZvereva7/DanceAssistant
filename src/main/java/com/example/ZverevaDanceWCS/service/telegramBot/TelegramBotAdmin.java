package com.example.ZverevaDanceWCS.service.telegramBot;

import com.example.ZverevaDanceWCS.service.Constant;
import com.example.ZverevaDanceWCS.service.model.exception.CommandNotRecognizedException;
import com.example.ZverevaDanceWCS.service.model.exception.WrongDateException;
import com.example.ZverevaDanceWCS.service.model.lessons.LessonService;
import com.example.ZverevaDanceWCS.service.model.lessons.LessonAdminDAO;
import com.example.ZverevaDanceWCS.service.model.lessons.LessonStatus;
import com.example.ZverevaDanceWCS.service.model.lessons.Lesson;
import com.example.ZverevaDanceWCS.service.model.user.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TelegramBotAdmin {

    @Autowired
    final UserService userService;
    @Autowired
    final LessonService lessonService;

    public TelegramBotAdmin(UserService userService, LessonService lessonService) {
        this.userService = userService;
        this.lessonService = lessonService;
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
       //menu.append("/payment:id-sum\n"); //todo
        menu.append("/balance:[id]\n");
        menu.append("/add_recap:lessonId-recap\n");

        return menu.toString();
    }

    public HashMap<Long, String> recap (String[] addInfo) { //add_recap:lessonId-recap
        HashMap<Long, String> response = new HashMap<>();
        if(addInfo!=null&&addInfo.length==2) {
            int lessonId = Integer.parseInt(addInfo[0]);
            Lesson lesson=lessonService.findById(lessonId);
            lesson.setRecap(addInfo[1]);
            lesson=lessonService.saveLesson(lesson);
            User student = lesson.getStudent();
            response.put(student.getChatId(), "New recap "+lesson.getDate()+": "+lesson.getRecap());
            response.put(Constant.adminChatId, "Recap added");
        } else {
            throw new CommandNotRecognizedException(Constant.CNR);
        }
        return response;
    }

    public String showBalance(String[] addInfo) {
        if(addInfo==null) {
            List<User> students = userService.findAll();
            return students.stream()
                    .filter(student->student.getBalance()!=0)
                    .map(UserShortDAO::new)
                    .map(UserShortDAO::stringBalance)
                    .collect(Collectors.joining("\n"));
        } else if(addInfo.length==1) {
            User student=userService.findById(Integer.parseInt(addInfo[0]));
            UserShortDAO userDao=new UserShortDAO(student);
            return userDao.stringBalance();
        } else {
            throw new CommandNotRecognizedException(Constant.CNR);
        }
    }

    public String showPlans(String[] addInfo) {
        if(addInfo==null) {
            List<User> students = userService.findAll();
            return students.stream()
                    .filter(student->student.getPlans()!=null)
                    .map(UserShortDAO::new)
                    .map(UserShortDAO::stringPlans)
                    .collect(Collectors.joining("\n"));
        } else if (addInfo.length==1) {
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
                    return "New role for "+student.getName()+": " +student.getRole();
                case "language":
                    //todo
                    return "";
                case "name":
                    student.setName(addInfo[2]);
                    student=userService.saveUser(student);
                    return "New name for "+student.getChatName()+": "+student.getName();
                case "plans" :
                    //todo check email
                    student.setPlans(addInfo[2]);
                    student=userService.saveUser(student);
                    return "New plans for "+student.getName();
                case "birthday":
                    //todo
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
            LocalDateTime newDateTime = LocalDateTime.parse(addInfo[2] , Constant.formatter);
            if (newDateTime.isBefore(LocalDateTime.now())) {
                throw new WrongDateException("New date must be in future");
            }
            Lesson lesson = lessonService.findByStudentDate(oldDate, studentId);
            if (lesson.getStatus() == LessonStatus.NEW || lesson.getStatus() == LessonStatus.PLANNED) {
                lesson.setDate(newDateTime);
                lesson.setStatus(LessonStatus.PLANNED);
                lessonService.saveLesson(lesson);
            }
            HashMap<Long, String> answers = new HashMap<>();
            answers.put(Constant.adminChatId, "Перенесен урок с " + oldDate.format(Constant.formatterJustDate) +
                    " на " + newDateTime.format(Constant.formatterTimeFirst) + " у " + student.getName());
            answers.put(student.getChatId(), "Date of lesson " + oldDate.format(Constant.formatterJustDate) +
                    " was changed. New date and time " + newDateTime.format(Constant.formatterTimeFirst));
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
                    .map(LessonAdminDAO::new)
                    .map(LessonAdminDAO::toString)
                    .collect(Collectors.joining("\n"));
        } else if (addInfo.length == 1) {
            answer = lessonService.findByStatusAndStudentName(LessonStatus.COMPLETED, addInfo[0])
                    .stream()
                    .map(LessonAdminDAO::new)
                    .map(LessonAdminDAO::toString)
                    .collect(Collectors.joining("\n"));
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
                        .map(LessonAdminDAO::new)
                        .map(LessonAdminDAO::toString)
                        .collect(Collectors.joining("\n"));
            } else {
                answer = lessonService.findInPeriod(LocalDate.now(), date).stream()
                        .map(LessonAdminDAO::new)
                        .map(LessonAdminDAO::toString)
                        .collect(Collectors.joining("\n"));
            }

        } else if (addInfo != null && addInfo.length == 2) {// две даты начала и конца временного промежутка
            answer = lessonService.findInPeriod(LocalDate.parse(addInfo[0], Constant.formatterJustDate),
                            LocalDate.parse(addInfo[1], Constant.formatterJustDate))
                    .stream()
                    .map(LessonAdminDAO::new)
                    .map(LessonAdminDAO::toString)
                    .collect(Collectors.joining("\n"));
        } else {
            answer = Constant.CNR;
        }
        return answer;
    }

    public HashMap<Long, String> lessonCompleted(String[] addInfo) {
        //adminChatId - answerAdmin
        //studentChatId - answerStudent
        //todo изменять баланс ученика при выполнении занятия
        HashMap<Long, String> answers = new HashMap<>();
        if (addInfo != null && addInfo.length == 1) {//только дата
            List<Lesson> lessons = lessonService.findByDate(LocalDate.parse((addInfo[0]), Constant.formatterJustDate));
            StringBuilder answerAdmin = new StringBuilder("Lessons completed: \n");
            for (Lesson lesson : lessons) {
                if (lesson.getStatus() == LessonStatus.NEW || lesson.getStatus() == LessonStatus.PLANNED) {
                    if (lesson.getStudent().getMessenger() == Messenger.TELEGRAM) {
                        answers.put(lesson.getStudent().getChatId(), "Lesson " + lesson.getDate().format(Constant.formatterJustDate) + " COMPLETED ("+lesson.getId()+")");
                    }
                    lesson.setStatus(LessonStatus.COMPLETED);
                    lesson.setCost();

                    User student = lesson.getStudent();
                    int balance =student.getBalance(); //todo check balance
                    balance=balance-lesson.getCost();
                    student.setBalance(balance);
                    userService.saveUser(student);

                    lessonService.saveLesson(lesson);
                    answerAdmin.append("- ")
                            .append(lesson.getStudent().getChatName())
                            .append(" - ")
                            .append(lesson.getDate().format(Constant.formatterJustDate))
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
                            .append(lesson.getDate().format(Constant.formatterJustDate))
                            .append("\n");
                }
            }
            answers.put(Constant.adminChatId, answerAdmin.toString());
        } else if (addInfo != null && addInfo.length == 2) {//конкретный пользователь
            Lesson lesson = lessonService.findByStudentDate(LocalDate.parse(addInfo[0], Constant.formatterJustDate), Integer.parseInt(addInfo[1]));
            lesson.setStatus(LessonStatus.COMPLETED);
            lesson.setCost();

            User student = lesson.getStudent();
            int balance =student.getBalance(); //todo check balance
            balance=balance-lesson.getCost();
            student.setBalance(balance);
            userService.saveUser(student);

            lessonService.saveLesson(lesson);
            answers.put(Constant.adminChatId, "Lesson completed: \n -" + lesson.getStudent().getChatName() + " - " + lesson.getDate().format(Constant.formatterJustDate));
            answers.put(lesson.getStudent().getChatId(), "Lesson " + lesson.getDate().format(Constant.formatterJustDate) + " completed ("+lesson.getId()+")");
        } else {
            answers.put(Constant.adminChatId, Constant.CNR);
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
            Lesson newLesson = addLesson(student, dateTime);
            return "New lesson added " + Constant.formatter.format(newLesson.getDate());
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

    private Lesson addLesson(User user, LocalDateTime dateTime) {
        Lesson newLesson = new Lesson();
        newLesson.setStudent(user);
        newLesson.setDate(dateTime);
        newLesson.setStatus(LessonStatus.PLANNED);
        return lessonService.saveLesson(newLesson);
    }

    public String schedule() {
        LocalDate now = LocalDate.now();
        return lessonService.findInPeriod(now, now.plusDays(10)).stream()
                .map(LessonAdminDAO::new)
                .map(LessonAdminDAO::toString)
                .collect(Collectors.joining("\n"));

    }

    //TODO проверка даты и даты со временем, происходит только по дате, без учета времени
    private boolean checkDate(LocalDateTime dateTime, int deltaInDays) {
        return dateTime.isBefore(LocalDateTime.now().plusDays(deltaInDays));
    }

}
