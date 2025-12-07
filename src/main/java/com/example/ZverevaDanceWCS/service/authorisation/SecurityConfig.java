package com.example.ZverevaDanceWCS.service.authorisation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomAuthSuccessHandler customAuthSuccessHandler;

    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService, CustomAuthSuccessHandler customAuthSuccessHandler) {
        this.customOAuth2UserService = customOAuth2UserService;
        this.customAuthSuccessHandler = customAuthSuccessHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/","/me", "/index.html","/guest.html").permitAll()
                        .requestMatchers("/admin/**", "admin.html").hasRole("ADMIN")
                        .requestMatchers("/user/**", "user.html").hasAnyRole("USER", "ADMIN")
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth
                        .loginPage("/index.html")
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .defaultSuccessUrl("/admin.html", true)
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/index")
                );
        log.info("SecurityConfig initialized");

        return http.build();
    }
}
