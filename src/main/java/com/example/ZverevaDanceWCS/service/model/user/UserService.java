package com.example.ZverevaDanceWCS.service.model.user;

import com.example.ZverevaDanceWCS.service.Constant;
import com.example.ZverevaDanceWCS.service.model.exception.NotFoundException;
import com.example.ZverevaDanceWCS.service.model.trainerStudentLink.TrainerStudentService;
import com.example.ZverevaDanceWCS.service.model.user.schedule.Schedule;
import com.example.ZverevaDanceWCS.service.model.user.studentInfo.InfoService;
import com.example.ZverevaDanceWCS.service.model.user.schedule.ScheduleService;
import com.example.ZverevaDanceWCS.service.model.user.userDTO.UserNewDTO;
import com.example.ZverevaDanceWCS.service.model.user.userDTO.UserUpdateByUserDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.google.common.graph.ElementOrder.sorted;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final InfoService infoService;
    private final ScheduleService scheduleService;
    private final TrainerStudentService trainerStudentService;

    public UserService(UserRepository userRepository, InfoService infoService, ScheduleService scheduleService, TrainerStudentService trainerStudentService) {
        this.userRepository = userRepository;
        this.infoService = infoService;
        this.scheduleService = scheduleService;
        this.trainerStudentService = trainerStudentService;
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
        newUser.setBirthday(LocalDate.parse(userNewDTO.getBirthday().formatted(Constant.formatterDayTime)));
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
        dto.setSchedules(scheduleService.findByStudent(user.getId())
                .stream()
                .map(Schedule::toShortDto)
                .toList());
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


    public List<User> findAllByTrainerId(int trainerId) {
        return trainerStudentService.getAllStudentsByTrainer(trainerId).stream().sorted(User.compareById()).toList();
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

    public List<User> findAllByRoleAndTrainerId(UserRole role, int trainerId) {
        if (role == null) {
            return trainerStudentService.getAllStudentsByTrainer(trainerId).stream().sorted(User.compareById()).toList();
        } else {
            return trainerStudentService.getAllStudentsByTrainer(trainerId).stream().filter(u->u.getRole()==role).sorted(User.compareById()).toList();
        }
    }

    public List<User> findAllByRoleInAndTrainerId(List<UserRole> roles, int trainerId) {
        return trainerStudentService.getAllStudentsByTrainer(trainerId).stream().filter(u->roles.contains(u.getRole())).sorted(User.compareById()).toList();
    }
}
