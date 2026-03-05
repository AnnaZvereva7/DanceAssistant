package com.example.ZverevaDanceWCS.service.model.tracks;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrackNewDto {
    String link;
    int bpm;
    String artist;
    String title;
    Set<String> tagNames;
}
