package com.example.ZverevaDanceWCS.service.model.calendarLink;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CalendarLinkRepository extends JpaRepository<CalendarLink, Long> {

    Optional<CalendarLink> findByTrainerIdAndExpires(Long trainerId, Boolean expires);
    Optional<CalendarLink> findByTokenAndExpires(String token, Boolean expires);
}
