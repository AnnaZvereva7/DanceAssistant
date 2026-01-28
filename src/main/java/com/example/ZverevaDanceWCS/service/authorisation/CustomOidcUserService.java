package com.example.ZverevaDanceWCS.service.authorisation;

import com.example.ZverevaDanceWCS.service.model.user.User;
import com.example.ZverevaDanceWCS.service.model.user.UserService;
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
    private final OidcUserService delegate = new OidcUserService();

    public CustomOidcUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) {
        OidcUser oidcUser = delegate.loadUser(userRequest);

        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();
        String sub = oidcUser.getSubject();

        User user = userService.findOrCreateNewUserFromGoogle(email, name);

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
