package com.example.ZverevaDanceWCS.service.model.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByChatName (String telegramUserName);
    Optional<User> findByChatId (long chatId);
    Optional<User> findById(int id);
    List<User> findAllByRole(UserRole role);
    List<User> findAll();
}
