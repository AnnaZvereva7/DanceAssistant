package com.example.ZverevaDanceWCS.service.authorisation;

import com.example.ZverevaDanceWCS.service.model.user.User;
import com.example.ZverevaDanceWCS.service.model.user.UserService;
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

    public CustomAuthSuccessHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        String email;
        String name;
        String sub;
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
        // Создаём/получаем HttpSession. Spring Session JDBC сохранит её в SQL.
        var session = request.getSession(true);
        session.setAttribute("USER_ID", user.getId());
        session.setAttribute("USER_STATUS", user.getUserSiteStatus().name());
        session.setAttribute("USER_NAME", user.getName());

        // Редирект на статические страницы
        String target = switch (user.getUserSiteStatus()) {
            case ADMIN -> "/trainer.html";
            case TRAINER -> "/trainer.html";   //todo separate trainer page
            case BLOCKED -> "/index.html";
            case ACTIVE -> "/user.html";
            default ->  "/guest.html";
        };

        response.sendRedirect(target);
    }
}
