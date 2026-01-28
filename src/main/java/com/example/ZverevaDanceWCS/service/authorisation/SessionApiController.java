package com.example.ZverevaDanceWCS.service.authorisation;

import com.example.ZverevaDanceWCS.service.model.user.User;
import com.example.ZverevaDanceWCS.service.model.user.UserRepository;
import com.example.ZverevaDanceWCS.service.model.user.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class SessionApiController {

    @GetMapping("/me")
    public Map<String, Object> me(HttpSession session) {
        Object userId = session.getAttribute("USER_ID");
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No session");
        }
        if(userId instanceof Integer){
            int id = (Integer) userId;
        } else {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid session data");
        }
        String userName = (String) session.getAttribute("USER_NAME");
        String status = (String) session.getAttribute("USER_STATUS");



        return Map.of(
                "userName", userName,
                "status", status
        );
    }
}
