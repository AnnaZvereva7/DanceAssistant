package com.example.ZverevaDanceWCS.service.model.tracks;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    Long id;

    @Column(name = "tag_name", unique = true, nullable = false)
    String tagName;

    @ManyToMany(mappedBy = "tags") //mappedBy = "tags" говорит, что owning side — это Track
    Set<Track> tracks = new HashSet<>();

}
