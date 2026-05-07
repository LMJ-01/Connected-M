package com.Connectedm.backend.domain.content.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
//@Table(name = "Content")
@Table(name = "content")
public class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "tmdb_id", nullable = false, unique = true, length = 50)
    private String tmdbId;

    @Column(name = "cine21_id", unique = true, length = 50)
    private String cine21Id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "overview", columnDefinition = "TEXT")
    private String overview;

    @Column(name = "poster_path")
    private String posterPath;

    @Column(name = "ott_logos")
    private String ottLogos;

    @Column(name = "backdrop_path")
    private String backdropPath;

    @Column(name = "runtime")
    private Integer runtime;

    @Column(name = "age_rating")
    private String ageRating;

    @Column(name = "view_count", columnDefinition = "INT DEFAULT 0")
    @Builder.Default
    private Integer viewCount = 0;

    @Column(name = "wish_count", columnDefinition = "INT DEFAULT 0")
    @Builder.Default
    private Integer wishCount = 0;

    // 조회수 증가 메서드
    public void incrementViewCount() {
        this.viewCount++;
    }

    // 장르와 연결 (1:N)
    @Builder.Default
    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, orphanRemoval = true )
    private List<ContentGenre> contentGenres = new ArrayList<>();

    // AI 분석 결과와의 연결 (1:1)
    @OneToOne(mappedBy = "content", cascade = CascadeType.ALL)
    private AnalysisCache analysisCache;

    // 장르 추가 시 양방향 매핑 메서드
    public void addGenre(ContentGenre contentGenre) {
        this.contentGenres.add(contentGenre);
        if (contentGenre.getContent() != this) {
            contentGenre.setContent(this);
        }
    }

    @Builder.Default
    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserReview> reviews = new ArrayList<>();

    /**
     * 1. TMDB 상세 정보 전용 업데이트 (가장 많이 사용)
     * tmdbId는 유지하고 내용물만 업데이트할 때 사용합니다.
     */
    public void updateTmdbInfo(String overview, String posterPath, String backdropPath, String ottLogos, Integer runtime, String ageRating) {
        this.overview = overview;
        this.posterPath = posterPath;
        this.backdropPath = backdropPath;
        this.ottLogos = ottLogos;
        this.runtime = runtime;
        this.ageRating = ageRating;
    }

    /**
     * 2. ID 교체 포함 전체 업데이트
     * 씨네21 ID를 TMDB ID로 바꾸면서 상세 정보까지 넣을 때 사용합니다.
     */
    public void updateFullInfo(String tmdbId, String overview, String posterPath, String backdropPath, String ottLogos, Integer runtime, String ageRating) {
        this.tmdbId = tmdbId;
        this.overview = overview;
        this.posterPath = posterPath;
        this.backdropPath = backdropPath;
        this.ottLogos = ottLogos;
        this.runtime = runtime;
        this.ageRating = ageRating;
    }

    // 리뷰 추가를 위한 메서드
    public void addReview(UserReview review) {
        this.reviews.add(review);
        if (review.getContent() != this) {
            review.setMapping(review.getUser(), this);
        }
    }
}
