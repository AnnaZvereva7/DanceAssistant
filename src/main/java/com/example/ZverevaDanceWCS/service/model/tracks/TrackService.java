package com.example.ZverevaDanceWCS.service.model.tracks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Service
@Slf4j
public class TrackService {
    final TrackRepository trackRepository;
    final TagRepository tagRepository;

    public TrackService(TrackRepository trackRepository, TagRepository tagRepository) {
        this.trackRepository = trackRepository;
        this.tagRepository = tagRepository;
    }

    public Track addNewTrack(TrackNewDto newTrack) {
        Track track = new Track();
        track.setSpotifyId(getSpotifyIdFromLink(newTrack.getLink()));
        track.setTrackTitle(newTrack.getTitle());
        track.setArtist(newTrack.getArtist());
        track.setBpm(newTrack.getBpm());
        track.setTags(getOrCreateTags(newTrack.getTagNames()));
        return trackRepository.save(track);
    }

    private String getSpotifyIdFromLink(String link) {
        //https://open.spotify.com/track/7EQjPEL70tzjlTgCgU0QrV?si=894c37aa65bd4929
        String clean = link.split("\\?")[0]; // убираем ?si=...
        return clean.substring(clean.lastIndexOf("/") + 1);
    }

    private Set<Tag> getOrCreateTags(Set<String> tagNames) {
        Set<Tag> tags = new HashSet<>();
        for (String tagName : tagNames) {
            Tag tag = tagRepository.findByTagName(tagName)
                    .orElseGet(() -> {
                        Tag newTag = new Tag();
                        newTag.setTagName(tagName);
                        return tagRepository.save(newTag);
                    });
            tags.add(tag);
        }
        return tags;
    }
}
