package com.example.ZverevaDanceWCS.service.model.calendarLink;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "trainer_calendar_links")
public class CalendarLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "link_id")
    Long linkId;

    @Column(name = "trainer_id", nullable = false)
    int trainerId;

    @Column(name = "token", nullable = false, unique = true)
    String token;

    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;

    @Column(name = "expires", nullable = false)
    Boolean expires;

    @Transient
    String link;

    public CalendarLink(int trainerId) {
        this.trainerId = trainerId;
        SecureRandom RNG = new SecureRandom();
        byte[] bytes = new byte[24];
        RNG.nextBytes(bytes);
        this.token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        this.createdAt = LocalDateTime.now();
        this.expires = false;
    }

    public String setLink(String publicUrl) {

        this.link = UriComponentsBuilder
                .fromHttpUrl(publicUrl)
                .path("/calendar.html")
                .queryParam("token", this.token)
                .toUriString();
        return this.link;
    }
}
