package com.example.ZverevaDanceWCS.service.model.calendarLink;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CalendarLinkService {
    final CalendarLinkRepository calendarLinkRepository;


    public CalendarLinkService(CalendarLinkRepository calendarLinkRepository) {
        this.calendarLinkRepository = calendarLinkRepository;
    }

    public CalendarLink findOrCreateLink(int trainerId) {
        Optional<CalendarLink> linkOpt = calendarLinkRepository.findByTrainerIdAndExpires(trainerId, false);
        return linkOpt.orElseGet(() -> calendarLinkRepository.save(new CalendarLink(trainerId)));
    }

    public CalendarLink refreshLink(int trainerId) {
        Optional<CalendarLink> linkOpt = calendarLinkRepository.findByTrainerIdAndExpires(trainerId, false);
        if(linkOpt.isPresent()) {
            CalendarLink link = linkOpt.get();
            link.setExpires(true);
            calendarLinkRepository.save(link);
        }
        return calendarLinkRepository.save(new CalendarLink(trainerId));
    }

    public int findTrainerIdByToken(String token) {
        Optional<CalendarLink> linkOpt = calendarLinkRepository.findTrainerIdByToken(token);
        if (linkOpt.isPresent()) {
           return linkOpt.get().getTrainerId();
        } else {
            throw new RuntimeException("Invalid token");
        }
    }
}
