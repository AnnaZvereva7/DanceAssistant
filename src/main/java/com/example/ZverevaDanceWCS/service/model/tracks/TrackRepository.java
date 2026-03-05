package com.example.ZverevaDanceWCS.service.model.tracks;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrackRepository extends JpaRepository<Track, Long> {
    Optional<Track> findBySpotifyId(String spotifyId);
    List<Track> findByBpmBetween(int minBpm, int maxBpm);
    List<Track> findByBpmBetweenAndTags_TagName(int minBpm, int maxBpm, String tagName);
}
