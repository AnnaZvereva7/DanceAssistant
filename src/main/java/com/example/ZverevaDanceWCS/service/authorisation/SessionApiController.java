package com.example.ZverevaDanceWCS.service.authorisation;

import com.example.ZverevaDanceWCS.service.model.user.User;
import com.example.ZverevaDanceWCS.service.model.user.UserRepository;
import com.example.ZverevaDanceWCS.service.model.user.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Controller
@Slf4j
public class SessionApiController {

    public static final String RETURN_TO_SESSION_KEY = "OAUTH_RETURN_TO";
    public static final String CALENDAR_TOKEN_SESSION_KEY = "CALENDAR_TOKEN";

    @GetMapping("/auth/login/google")
    public String startGoogle(@RequestParam("returnTo") String returnTo,
                              @RequestParam(value = "calendarToken", required = false) String calendarToken,
                              HttpSession session) {
        log.info("Received login request with returnTo: {} and calendarToken: {}", returnTo, calendarToken);
        session.setAttribute(RETURN_TO_SESSION_KEY, returnTo);
        if (calendarToken != null && !calendarToken.isBlank()) {
            session.setAttribute(CALENDAR_TOKEN_SESSION_KEY, calendarToken);
        }
        return "redirect:/oauth2/authorization/google";
    }

    @GetMapping("/api/me")
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
