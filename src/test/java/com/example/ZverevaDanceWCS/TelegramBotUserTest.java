package com.example.ZverevaDanceWCS;

import com.example.ZverevaDanceWCS.service.model.lessons.LessonService;
import com.example.ZverevaDanceWCS.service.model.user.UserService;
import com.example.ZverevaDanceWCS.service.telegramBot.TelegramBotUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TelegramBotUserTest {

    @Mock
    private UserService userService;

    @Mock
    private LessonService lessonService;

    @InjectMocks
    private TelegramBotUser botUser;

    @Test
    void startCommandRecieveUser_ChatName_NULL() {

    }
}
