package com.Connectedm.backend.domain.content.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "cine21_source")
public class Cine21Source {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_movie_id", nullable = false, unique = true, length = 50)
    private String externalMovieId;

    @Column(name = "raw_title", nullable = false, length = 250)
    private String rawTitle;

    @Column(name = "crawl_date")
    private LocalDateTime crawlDate;

    @Builder.Default
    @OneToMany(mappedBy = "cine21Source", cascade = CascadeType.ALL)
    private List<ExpertReview> expertReviews = new ArrayList<>();
}
