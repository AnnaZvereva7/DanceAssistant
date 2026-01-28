package com.example.ZverevaDanceWCS.service.model.user;

import com.example.ZverevaDanceWCS.service.Constant;
import com.example.ZverevaDanceWCS.service.model.exception.NotFoundException;
import com.example.ZverevaDanceWCS.service.model.studentInfo.InfoService;
import com.example.ZverevaDanceWCS.service.model.user.userDTO.UserNewDTO;
import com.example.ZverevaDanceWCS.service.model.user.userDTO.UserUpdateByUserDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public User newUser(String name) {
        User newUser = new User();
        newUser.setName(name);
        newUser.setRole(UserRole.BY_REQUEST);
        newUser.setBalance(0);
        return userRepository.save(newUser);
    }

    public User newUserTelegram(long chatId, String name, String chatName) {
        User newUser = new User();
        if (name != null) {
            newUser.setName(name);
        }
        newUser.setChatName(chatName);
        newUser.setChatId(chatId);
        newUser.setRole(UserRole.BY_REQUEST);
        newUser.setMessenger(Messenger.TELEGRAM);
        newUser.setBalance(0);
        newUser.setUserSiteStatus(UserSiteStatus.ACTIVE);
        return userRepository.save(newUser);
    }

    public User newUserFromJson(UserNewDTO userNewDTO) {
        User newUser = new User();
        newUser.setName(userNewDTO.getName());
        newUser.setChatName(userNewDTO.getName());
        newUser.setEmail(userNewDTO.getEmail());
        newUser.setRole(UserRole.BY_REQUEST);
        newUser.setMessenger(Messenger.NONE);
        newUser.setBalance(0);
        newUser.setLanguage(Language.ENG);
        newUser.setBirthday(LocalDate.parse(userNewDTO.getBirthday().formatted(Constant.formatter)));
        newUser.setUserSiteStatus(UserSiteStatus.ACTIVE);
        return userRepository.save(newUser);
    }

    public UserUpdateByUserDto updateByUser(UserUpdateByUserDto dto, int userId) {
        User user = findByIdWithInfo(userId);
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setBirthday(dto.getBirthday());
        userRepository.save(user);
        dto.setAdditionalInfo(user.getAdditionalInfo());
        dto.setSchedule(user.getScheduleDay() != null && user.getScheduleTime() != null
                ? user.getScheduleDay().toString() + " " + user.getScheduleTime().toString()
                : "Not set");
        return dto;
    }


    @Transactional
    public User findOrCreateNewUserFromGoogle(String email, String name) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            return userOptional.get();
        } else {
            User newUser = new User();
            newUser.setName(name);
            newUser.setChatName(name);
            newUser.setEmail(email);
            newUser.setRole(UserRole.BY_REQUEST);
            newUser.setMessenger(Messenger.NONE);
            newUser.setBalance(0);
            newUser.setLanguage(Language.ENG);
            newUser.setUserSiteStatus(UserSiteStatus.ACTIVE);
            return userRepository.save(newUser);
        }
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
        log.info("Loading plans for student id=" + id+": "+student.getAdditionalInfo());
        String plans = infoService.findByStudentActual(id);
        log.info("Found plans: "+plans);
        student.setAdditionalInfo(plans);
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
