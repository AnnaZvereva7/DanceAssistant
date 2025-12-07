package com.example.ZverevaDanceWCS.service.controller;

import com.example.ZverevaDanceWCS.service.model.lessons.lessonDTO.LessonFullDTO;
import com.example.ZverevaDanceWCS.service.model.lessons.LessonService;
import com.example.ZverevaDanceWCS.service.model.user.UserService;
import com.example.ZverevaDanceWCS.service.model.user.userDTO.UserUpdateDto;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController("/user")
public class UserController {
    final LessonService lessonService;
    final UserService userService;

    public UserController(LessonService lessonService, UserService userService) {
        this.lessonService = lessonService;
        this.userService = userService;
    }

    //User endpoint, update details
    @PutMapping("/change_user/{id}")
    public void updateUserDetails(@Valid @RequestBody UserUpdateDto userUpdateDto) {

    }

    //User and Admin endpoint
    @GetMapping("/lesson/{id}")
    public LessonFullDTO getLessonById(@PathVariable int id) {
        return LessonFullDTO.toFullDTO(lessonService.findById(id));
    }

    //User endpoint, get details
    @GetMapping("/find_user/{id}")
    public void getUserDetails(@PathVariable int id) {

    }

}
