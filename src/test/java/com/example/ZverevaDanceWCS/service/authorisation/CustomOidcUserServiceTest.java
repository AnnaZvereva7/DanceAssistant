package com.example.ZverevaDanceWCS.service.authorisation;

import com.example.ZverevaDanceWCS.service.model.calendarLink.CalendarLinkService;
import com.example.ZverevaDanceWCS.service.model.user.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class CustomOidcUserServiceTest {
    private final UserService userService = mock(UserService.class);
    private final CalendarLinkService calendarLinkService = mock(CalendarLinkService.class);
    private final OidcUserService delegate = mock(OidcUserService.class);
    private final CustomOidcUserService service = new CustomOidcUserService(userService, calendarLinkService);
    private final OidcUserRequest userRequest = mock(OidcUserRequest.class);

    @AfterEach
    void clearRequest() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void newUserCannotSignInWithoutTrainerLink() {
        prepareGoogleUser();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));

        assertThrows(OAuth2AuthenticationException.class, () -> service.loadUser(userRequest));
        verify(userService, never()).findOrCreateNewUserFromGoogle(anyString(), anyString());
    }

    @Test
    void invalidTrainerLinkDoesNotCreateUser() {
        prepareGoogleUser();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession().setAttribute(SessionApiController.CALENDAR_TOKEN_SESSION_KEY, "invalid");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        when(calendarLinkService.findTrainerIdByToken("invalid")).thenThrow(new IllegalArgumentException("Invalid token"));

        assertThrows(OAuth2AuthenticationException.class, () -> service.loadUser(userRequest));
        verify(userService, never()).findOrCreateNewUserFromGoogle(anyString(), anyString());
    }

    private void prepareGoogleUser() {
        ReflectionTestUtils.setField(service, "delegate", delegate);
        OidcUser googleUser = mock(OidcUser.class);
        when(googleUser.getEmail()).thenReturn("new@example.com");
        when(googleUser.getFullName()).thenReturn("New User");
        when(delegate.loadUser(userRequest)).thenReturn(googleUser);
        when(userService.findByEmail("new@example.com")).thenReturn(Optional.empty());
    }
}
