package com.example.ZverevaDanceWCS.service.model.user;

import com.example.ZverevaDanceWCS.service.Constant;
import com.example.ZverevaDanceWCS.service.model.exception.NotFoundException;
import com.example.ZverevaDanceWCS.service.model.studentInfo.InfoService;
import com.example.ZverevaDanceWCS.service.model.user.userDTO.UserNewDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class UserService {

    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final InfoService infoService;

    public UserService(UserRepository userRepository, InfoService infoService) {
        this.userRepository = userRepository;
        this.infoService = infoService;
    }

    public User newUserTelegram(long chatId, String name, String chatName) {
        User newUser = new User();
        if (name != null) {
            newUser.setName(name);
        }
        newUser.setChatName(chatName);
        newUser.setChatId(chatId);
        newUser.setRole(UserRole.NEW);
        newUser.setMessenger(Messenger.TELEGRAM);
        newUser.setBalance(0);
        return userRepository.save(newUser);
    }

    public User newUserFromJson(UserNewDTO userNewDTO) {
        User newUser = new User();
        newUser.setName(userNewDTO.getName());
        newUser.setChatName(userNewDTO.getName());
        newUser.setEmail(userNewDTO.getEmail());
        newUser.setRole(UserRole.NEW);
        newUser.setMessenger(Messenger.WHATSAPP);
        newUser.setBalance(0);
        newUser.setLanguage(Language.ENG);
        newUser.setBirthday(LocalDate.parse(userNewDTO.getBirthday().formatted(Constant.formatter)));
        return userRepository.save(newUser);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public Optional<User> findByChatName(String chatName) {
        return userRepository.findByChatName(chatName);
    }

    public User findByChatId(long chatId) {
        if (userRepository.findByChatId(chatId).isPresent()) {
            return userRepository.findByChatId(chatId).get();
        } else {
            throw new NotFoundException("User with this chatId not found");
        }
    }

    public User findById(int id) {
        if (userRepository.findById(id).isPresent()) {
            return userRepository.findById(id).get();
        } else {
            throw new NotFoundException("User with id=" + id + " not found");
        }
    }

    public User findByIdWithInfo(int id) {
        User student = findById(id);
        log.info("Loading plans for student id=" + id+": "+student.getPlans());
        String plans = infoService.findByStudentActual(id);
        log.info("Found plans: "+plans);
        student.setPlans(plans);
        return student;
    }

    public List<User> findAllByRole(UserRole role) {
        if (role == null) {
            return userRepository.findAll().stream().sorted(User.compareById()).toList();
        } else {
            return userRepository.findAllByRole(role).stream().sorted(User.compareById()).toList();
        }
    }

    public List<User> usersWithSchedule() {
        return userRepository.findByScheduleDayNotNull();
    }

    public List<User> findAllByRoleIn(List<UserRole> roles) {
        return userRepository.findAllByRoleIn(roles);
    }
}
