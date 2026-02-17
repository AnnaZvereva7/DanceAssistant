package com.example.ZverevaDanceWCS.service.authorisation;

import com.example.ZverevaDanceWCS.service.model.calendarLink.CalendarLinkService;
import com.example.ZverevaDanceWCS.service.model.trainerStudentLink.TrainerStudentService;
import com.example.ZverevaDanceWCS.service.model.user.User;
import com.example.ZverevaDanceWCS.service.model.user.UserService;
import com.example.ZverevaDanceWCS.service.model.user.UserSiteStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class CustomAuthSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;
    private final CalendarLinkService calendarLinkService;
    private final TrainerStudentService trainerStudentService;
    // public static final String RETURN_TO_SESSION_KEY = "OAUTH_RETURN_TO";

    public CustomAuthSuccessHandler(UserService userService, CalendarLinkService calendarLinkService, TrainerStudentService trainerStudentService) {
        this.userService = userService;
        this.calendarLinkService = calendarLinkService;
        this.trainerStudentService = trainerStudentService;
    }


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        String email;
        String name;
        Object principal = token.getPrincipal();
        if (principal instanceof OidcUser oidc) {
            email = oidc.getEmail();
            name = oidc.getFullName();
        } else {
            var attrs = token.getPrincipal().getAttributes();
            email = (String) attrs.get("email");
            name  = (String) attrs.get("name");
        }
        User user = userService.findOrCreateNewUserFromGoogle(email, name);
        if (user.getUserSiteStatus() == UserSiteStatus.BLOCKED) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "User is blocked");
            return;
        }
        // Создаём/получаем HttpSession. Spring Session JDBC сохранит её в SQL.
        var session = request.getSession(true);
        session.setAttribute("USER_ID", user.getId());
        session.setAttribute("USER_STATUS", user.getUserSiteStatus().name());
        session.setAttribute("USER_NAME", user.getName());

        String calendarToken = (String) session.getAttribute(SessionApiController.CALENDAR_TOKEN_SESSION_KEY);
        log.info("Calendar token from session: {}", calendarToken);
        session.removeAttribute(SessionApiController.CALENDAR_TOKEN_SESSION_KEY);

        if (calendarToken != null && !calendarToken.isBlank()) {
            try {
                int trainerId = calendarLinkService.findTrainerIdByToken(calendarToken);
                User trainer = userService.findById(trainerId);
                trainerStudentService.saveLink(trainer, user);
            } catch (RuntimeException ex) {
                throw new RuntimeException("The link is no longer valid.");
            }
        }

        String returnTo = (String) session.getAttribute(SessionApiController.RETURN_TO_SESSION_KEY);
        log.info("ReturnTo from session: {}", returnTo);
        session.removeAttribute(SessionApiController.RETURN_TO_SESSION_KEY);

        if (returnTo != null && !returnTo.isBlank()) {
            log.info("Redirecting back to: {}", returnTo);
            response.sendRedirect(returnTo);
            return;
        }

        // если returnTo нет — используем стандартную логику
        String target = switch (user.getUserSiteStatus()) {
            case ADMIN -> "/trainer.html";
            case TRAINER -> "/trainer.html";   //todo separate trainer page
            case BLOCKED -> "/index.html";
            case ACTIVE -> "/user.html";
            default ->  "/index.html";
        };

        response.sendRedirect(target);
    }
}
