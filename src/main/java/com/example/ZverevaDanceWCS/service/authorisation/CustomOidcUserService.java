package com.example.ZverevaDanceWCS.service.authorisation;

import com.example.ZverevaDanceWCS.service.model.user.User;
import com.example.ZverevaDanceWCS.service.model.user.UserService;
import com.example.ZverevaDanceWCS.service.model.calendarLink.CalendarLinkService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {
    private final UserService userService;
    private final CalendarLinkService calendarLinkService;
    private final OidcUserService delegate = new OidcUserService();

    public CustomOidcUserService(UserService userService, CalendarLinkService calendarLinkService) {
        this.userService = userService;
        this.calendarLinkService = calendarLinkService;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) {
        OidcUser oidcUser = delegate.loadUser(userRequest);

        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();
        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException("Google account has no email address");
        }
        User user = userService.findByEmail(email).orElseGet(() -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes == null ? null : attributes.getRequest();
            String calendarToken = request == null ? null :
                    (String) request.getSession().getAttribute(SessionApiController.CALENDAR_TOKEN_SESSION_KEY);
            if (calendarToken == null || calendarToken.isBlank()) {
                throw new OAuth2AuthenticationException("A trainer link is required for a new account");
            }
            try {
                calendarLinkService.findTrainerIdByToken(calendarToken);
            } catch (RuntimeException ex) {
                throw new OAuth2AuthenticationException("The trainer link is invalid or expired");
            }
            return userService.findOrCreateNewUserFromGoogle(email, name);
        });

        // Маппим твой статус в роли Spring Security
        List<GrantedAuthority> authorities = new ArrayList<>();
        switch (user.getUserSiteStatus()) {
            case ADMIN -> authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            case TRAINER -> authorities.add(new SimpleGrantedAuthority("ROLE_TRAINER"));
            case ACTIVE -> authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            default -> authorities.add(new SimpleGrantedAuthority("ROLE_GUEST"));
        }


        // Важно: nameAttributeKey обычно "sub"
        return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo(), "sub");
    }
}
