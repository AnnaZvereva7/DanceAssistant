package com.example.ZverevaDanceWCS.service.authorisation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final CustomOidcUserService customOidcUserService;
    private final CustomAuthSuccessHandler successHandler;

    public SecurityConfig(CustomOidcUserService customOidcUserService, CustomAuthSuccessHandler successHandler) {
        this.customOidcUserService = customOidcUserService;
        this.successHandler = successHandler;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        .ignoringRequestMatchers("/logout")
                )
                .addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class)
                .authorizeHttpRequests(auth -> auth
                        // публичные ресурсы
                        .requestMatchers("/", "/index.html", "/css/**", "/js/**", "/images/**", "/guest.html").permitAll()

                        // OAuth endpoints
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                        .requestMatchers("/error", "/favicon.ico").permitAll()

                        // защищённые статические страницы
                        .requestMatchers("/trainer.html").hasAnyRole("TRAINER", "ADMIN")
                        .requestMatchers("/admin.html").hasRole("ADMIN")
                        .requestMatchers("/user.html").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/calendar.html", "/calendar/**").permitAll() //todo может только для зарегистрированных? но скорее смотреть можно без регистрации, а букать с регистрацией

                        // API тоже защищаем
                        .requestMatchers("/api/**").authenticated()

                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth
                        // твоя статическая страница логина
                        .loginPage("/index.html")
                        .userInfoEndpoint(u -> u.oidcUserService(customOidcUserService))
                        .successHandler(successHandler)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/index.html")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("SESSION")
                );

        return http.build();
    }
}
