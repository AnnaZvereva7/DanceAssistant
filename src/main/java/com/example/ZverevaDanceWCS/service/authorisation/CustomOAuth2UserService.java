package com.example.ZverevaDanceWCS.service.authorisation;

import com.example.ZverevaDanceWCS.service.model.user.User;
import com.example.ZverevaDanceWCS.service.model.user.UserRepository;
import com.example.ZverevaDanceWCS.service.model.user.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauthUser = super.loadUser(userRequest);

        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");
log.info("check user");
        // 1. Ищем пользователя по email если нет - создаем нового
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User u = new User();
                    u.setEmail(email);
                    u.setName(name);
                    u.setRole(UserRole.NEW);  // по умолчанию обычный пользователь
                    u.setBalance(0);
                    return userRepository.save(u);
                });

        // 2. Достаём роль из БД и превращаем в GrantedAuthority
        Collection<GrantedAuthority> authorities = null;
        if(user.getRole()==UserRole.ADMIN) {
            log.info("admin logged in:" + email);
            authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else {
            log.info("user logged in:" + email);
            authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }
        return new DefaultOAuth2User(authorities, oauthUser.getAttributes(), "email");
    }
}
