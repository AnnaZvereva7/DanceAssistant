package com.example.ZverevaDanceWCS.service.model.user;

import com.example.ZverevaDanceWCS.service.model.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private final UserRepository userRepository;

    public UserService (UserRepository userRepository) {
        this.userRepository=userRepository;
    }

    public User newUserTelegram(long chatId, String name, String chatName) {
        User newUser = new User();
        if(name!=null) {
            newUser.setName(name);
        }
        newUser.setChatName(chatName);
        newUser.setChatId(chatId);
        newUser.setRole(UserRole.NEW);
        newUser.setMessenger(Messenger.TELEGRAM);
        newUser.setBalance(0);
        return userRepository.save(newUser);
    }

 public List<User> findAll() {
        return userRepository.findAll();
 }

    public User saveUser (User user) {
       return userRepository.save(user);
    }

    public Optional<User> findByChatName (String chatName) {
        return userRepository.findByChatName(chatName);
    }

    public User findByChatId (long chatId) {
        if(userRepository.findByChatId(chatId).isPresent()) {
            return userRepository.findByChatId(chatId).get();
        } else {
            throw new NotFoundException("User with this chatId not found");
        }
    }

    public User findById (int id) {
        if(userRepository.findById(id).isPresent()) {
            return userRepository.findById(id).get();
        } else {
            throw new NotFoundException("User with id="+id+" not found");
        }
    }

    public List<User> findAllByRole (UserRole role) {
        if(role==null) {
            return userRepository.findAll();
        } else {
            return userRepository.findAllByRole(role);
        }
    }

    public List<User> usersWithSchedule() {
        return userRepository.findByScheduleDayNotNull();
    }

}
