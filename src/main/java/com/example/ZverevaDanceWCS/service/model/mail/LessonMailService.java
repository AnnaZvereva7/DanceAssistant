package com.example.ZverevaDanceWCS.service.model.mail;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class LessonMailService {
    private final JavaMailSender mailSender;

    public LessonMailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
}
