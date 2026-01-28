package com.example.ZverevaDanceWCS.service.controller;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class GuestController {
    @GetMapping("/me")
    public Map<String, Object> me(OAuth2AuthenticationToken auth) {
        if (auth == null) {
            return Map.of("authenticated", false);
        }

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return Map.of(
                "authenticated", true,
                "email", auth.getPrincipal().getAttribute("email"),
                "name",  auth.getPrincipal().getAttribute("name"),
                "admin", isAdmin
        );
    }
}
