package com.Connectedm.backend.domain.content.service;

import com.Connectedm.backend.domain.content.dto.TmdbMovieResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TmdbService {

    @Value("${tmdb.api.key}")
    private String apiKey;

    @Value("${tmdb.api.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 영화 제목으로 TMDB ID 검색
     */
    public Long searchMovieIdByTitle(String title) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/search/movie")
                .queryParam("api_key", apiKey)
                .queryParam("query", title)
                .queryParam("language", "ko-KR")
                .toUriString();

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        if (response != null && response.get("results") != null) {
            List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
            if (!results.isEmpty()) {
                return Long.valueOf(results.get(0).get("id").toString());
            }
        }
        return null;
    }

    /**
     * 영화 상세 정보 및 한국 심의 등급 가져오기
     */
    public TmdbMovieResponseDto getMovieDetail(String tmdbId) {
        String url = UriComponentsBuilder.fromHttpUrl("https://api.themoviedb.org/3/movie/" + tmdbId)
                .queryParam("api_key", apiKey)
                .queryParam("language", "ko-KR")
                .queryParam("append_to_response", "credits,watch/providers,release_dates,videos")
                .toUriString();

        TmdbMovieResponseDto response = restTemplate.getForObject(url, TmdbMovieResponseDto.class);

        // KR 등급 추출 로직
        if (response != null && response.getReleaseDates() != null && response.getReleaseDates().getResults() != null) {
            String krRating = response.getReleaseDates().getResults().stream()
                    // 1. 한국(KR) 데이터 필터링 + 내부 리스트가 null이 아닌지 확인
                    .filter(r -> "KR".equals(r.getIso31661()) && r.getReleaseDates() != null)
                    // 2. 내부의 release_dates 리스트를 평탄화 (Stream<ReleaseDateDetail>)
                    .flatMap(r -> r.getReleaseDates().stream())
                    // 3. certification(ageRating) 값 추출
                    .map(TmdbMovieResponseDto.ReleaseDateDetail::getAgeRating)
                    // 4. 비어있지 않은 값만 필터링
                    .filter(c -> c != null && !c.isEmpty())
                    .findFirst()
                    .orElse("정보없음");

            response.setAgeRating(krRating);
        }

        // 트레일러 키 추출
        if (response != null && response.getVideos() != null && response.getVideos().getResults() != null) {
            String trailerKey = response.getVideos().getResults().stream()
                    .filter(v -> "YouTube".equals(v.getSite()) && "Trailer".equals(v.getType()))
                    .map(TmdbMovieResponseDto.TmdbVideoItem::getKey)
                    .findFirst()
                    .orElse(null);

            response.setTrailerKey(trailerKey);
        }

        return response;
    }
}