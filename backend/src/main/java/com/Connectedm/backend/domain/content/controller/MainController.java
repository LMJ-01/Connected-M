package com.Connectedm.backend.domain.content.controller;

import com.Connectedm.backend.domain.content.dto.MovieMainDto;
import com.Connectedm.backend.domain.content.entity.Content;
import com.Connectedm.backend.domain.content.entity.Genre;
import com.Connectedm.backend.domain.content.repository.ContentRepository;
import com.Connectedm.backend.domain.content.repository.ExpertReviewRepository;
import com.Connectedm.backend.domain.content.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/main")
@RequiredArgsConstructor
public class MainController {

    private final ContentRepository contentRepository;
    private final GenreRepository genreRepository;
    private final ExpertReviewRepository expertReviewRepository;


    // 1. 장르 목록 (카테고리)
    @GetMapping("/genres")
    public ResponseEntity<List<Genre>> getGenres() {
        return ResponseEntity.ok(genreRepository.findAll());
    }

    // 2. 오늘의 추천작 10개
    @GetMapping("/today")
    public ResponseEntity<List<MovieMainDto>> getTodayMovies() {
        List<Content> contents = contentRepository.findTop10ByOrderByIdDesc();
        return ResponseEntity.ok(convertToDtoList(contents));
    }

    // 내부 변환 로직 (엔티티 -> DTO)
    private List<MovieMainDto> convertToDtoList(List<Content> contents) {
        return contents.stream().map(c -> {
            try {
                // 평점 가져오기 시도
                Double avgRating = expertReviewRepository.getAverageRatingByContentId(c.getId());
//                double formattedRating = (avgRating != null) ? Math.round(avgRating * 10) / 10.0 : 0.0;

                return MovieMainDto.builder()
                        .id(c.getId())
                        .title(c.getTitle())
                        .posterPath(c.getPosterPath())
                        .averageRating(0.0)
                        .build();
            } catch (Exception e) {
                // 🔥 여기서 에러가 나면 콘솔에 범인을 찍어줍니다!
                System.err.println("에러 발생 영화 ID: " + c.getId());
                e.printStackTrace();
                throw e;
            }
        }).collect(Collectors.toList());
    }
}
