package com.example.ZverevaDanceWCS.service.model.user;

import com.example.ZverevaDanceWCS.service.Constant;
import com.example.ZverevaDanceWCS.service.model.calendarLink.CalendarLinkService;
import com.example.ZverevaDanceWCS.service.model.exception.NotFoundException;
import com.example.ZverevaDanceWCS.service.model.trainerStudentLink.TrainerStudentService;
import com.example.ZverevaDanceWCS.service.model.user.schedule.Schedule;
import com.example.ZverevaDanceWCS.service.model.user.studentInfo.InfoService;
import com.example.ZverevaDanceWCS.service.model.user.schedule.ScheduleService;
import com.example.ZverevaDanceWCS.service.model.user.userDTO.TrainerInfoDTO;
import com.example.ZverevaDanceWCS.service.model.user.userDTO.UserNewDTO;
import com.example.ZverevaDanceWCS.service.model.user.userDTO.UserUpdateByUserDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Slf4j
@Service
@PropertySource("application.properties")
public class UserService {

    private final UserRepository userRepository;
    private final InfoService infoService;
    private final ScheduleService scheduleService;
    private final TrainerStudentService trainerStudentService;
    private final CalendarLinkService calendarLinkService;
    @Value("${public.url}")
    private String publicUrl;



    public UserService(UserRepository userRepository, InfoService infoService, ScheduleService scheduleService, TrainerStudentService trainerStudentService, CalendarLinkService calendarLinkService) {
        this.userRepository = userRepository;
        this.infoService = infoService;
        this.scheduleService = scheduleService;
        this.trainerStudentService = trainerStudentService;
        this.calendarLinkService = calendarLinkService;
    }

    public User newUser(String name) {
        User newUser = new User();
        newUser.setName(name);
        newUser.setRole(UserRole.BY_REQUEST);
        newUser.setBalance(0);
        newUser.setMessenger(Messenger.NONE);
        newUser.setUserSiteStatus(UserSiteStatus.ACTIVE);
        return userRepository.save(newUser);
    }

    public UserUpdateByUserDto updateByUser(UserUpdateByUserDto dto, Long userId) {
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

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public User findByChatId(long chatId) {
        if (userRepository.findByChatId(chatId).isPresent()) {
            return userRepository.findByChatId(chatId).get();
        } else {
            throw new NotFoundException("User with this chatId not found");
        }
    }

    public User findById(Long id) {
        if (userRepository.findById(id).isPresent()) {
            return userRepository.findById(id).get();
        } else {
            throw new NotFoundException("User with id=" + id + " not found");
        }
    }

    public User findByIdWithInfo(Long id) {
        User student = findById(id);
        log.info("Loading plans for student id=" + id+": "+student.getAdditionalInfo());
        String plans = infoService.findByStudentActual(id);
        log.info("Found plans: "+plans);
        student.setAdditionalInfo(plans);
        return student;
    }

    public List<User> findAllByRoleInAndTrainerId(List<UserRole> roles, Long trainerId) {
        return trainerStudentService.getAllStudentsByTrainer(trainerId).stream().filter(u->roles.contains(u.getRole())).sorted(User.compareById()).toList();
    }

     public TrainerInfoDTO getTrainerInfo(User trainer) {
         String calendarLink = calendarLinkService.findOrCreateLink(trainer.getId()).setLink(publicUrl);
         log.info("Generated calendar link for trainer id=" + trainer.getName() + " link: " + calendarLink);
         return new TrainerInfoDTO(trainer.getName(), trainer.getEmail(), calendarLink);
     }

     public TrainerInfoDTO refreshCalendarLink(User trainer) {
         String calendarLink = calendarLinkService.refreshLink(trainer.getId()).setLink(publicUrl);
         return new TrainerInfoDTO(trainer.getName(), trainer.getEmail(), calendarLink);
     }
}
