package com.example.ZverevaDanceWCS.service.model.calendarLink;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CalendarLinkService {
    final CalendarLinkRepository calendarLinkRepository;


    public CalendarLinkService(CalendarLinkRepository calendarLinkRepository) {
        this.calendarLinkRepository = calendarLinkRepository;
    }

    public CalendarLink findOrCreateLink(Long trainerId) {
        Optional<CalendarLink> linkOpt = calendarLinkRepository.findByTrainerIdAndExpires(trainerId, false);
        return linkOpt.orElseGet(() -> calendarLinkRepository.save(new CalendarLink(trainerId)));
    }

    public CalendarLink refreshLink(Long trainerId) {
        Optional<CalendarLink> linkOpt = calendarLinkRepository.findByTrainerIdAndExpires(trainerId, false);
        if(linkOpt.isPresent()) {
            CalendarLink link = linkOpt.get();
            link.setExpires(true);
            calendarLinkRepository.save(link);
        }
        return calendarLinkRepository.save(new CalendarLink(trainerId));
    }

    public Long findTrainerIdByToken(String token) {
        Optional<CalendarLink> linkOpt = calendarLinkRepository.findByTokenAndExpires(token, false);
        if (linkOpt.isPresent()) {
           return linkOpt.get().getTrainerId();
        } else {
            throw new RuntimeException("Invalid token");
        }
    }
}
