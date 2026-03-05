package com.example.ZverevaDanceWCS.service.controller;

import com.example.ZverevaDanceWCS.service.model.tracks.TrackNewDto;
import com.example.ZverevaDanceWCS.service.model.tracks.TrackService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/music")
@PreAuthorize("hasRole('ADMIN')")
public class MusicController {
    final TrackService trackService;

    public MusicController(TrackService trackService) {
        this.trackService = trackService;
    }

    @PostMapping("/new_track")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addNewTrack(@RequestBody TrackNewDto newTrack) {
        trackService.addNewTrack(newTrack);
    }
}
