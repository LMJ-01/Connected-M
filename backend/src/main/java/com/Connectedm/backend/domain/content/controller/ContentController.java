package com.Connectedm.backend.domain.content.controller;

import com.Connectedm.backend.domain.content.dto.*;
import com.Connectedm.backend.domain.content.service.ContentService;
import com.Connectedm.backend.domain.content.service.ReviewService;
import com.Connectedm.backend.domain.content.service.SemanticSearchService;
import com.Connectedm.backend.domain.content.service.SearchService;
import com.Connectedm.backend.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;
    private final ReviewService reviewService;
    private final SemanticSearchService semanticSearchService;
    private final SearchService searchService;
    private final RestTemplate restTemplate;

    /**
     * 메인페이지 데이터 조회
     */
    @GetMapping("/main")
    public ApiResponse<MainPageResponseDto> getMainPage() {
        return ApiResponse.success(contentService.getMainPageData());
    }

    /**
     * 상세페이지 데이터 조회
     */
    @GetMapping("/{id}")
    public ApiResponse<ContentDetailResponseDto> getContentDetail(@PathVariable("id") Long id) {
        ContentDetailResponseDto detail = contentService.getContentDetail(id);

        // 줄거리나 장르가 비어있으면 TMDB 동기화 진행
        if (detail.getOverview() == null || detail.getOverview().isBlank() ||
                detail.getGenres().isEmpty() ||
                detail.getRuntime() == null ||
                detail.getAgeRating() == null ||
                detail.getAgeRating().equals("정보없음")
        ) {
            contentService.updateContentWithTmdb(id);
            detail = contentService.getContentDetail(id);
        }

        return ApiResponse.success(detail);
    }

    /**
     * [전문가 리뷰] 전체 조회
     */
    @GetMapping("/reviews")
    public ApiResponse<List<ReviewResponseDto>> getAllExpertReviews() {
        return ApiResponse.success(reviewService.getAllExpertReviews());
    }

    /**
     * [전문가 리뷰] 크롤링 데이터 수신 전용 POST
     */
    @PostMapping("/reviews")
    public ApiResponse<String> saveExpertReview(@RequestBody ExpertReviewCreateRequestDto dto) {
        reviewService.saveExpertReview(dto);
        return ApiResponse.success("전문가 리뷰 저장 완료!");
    }

    @PostMapping("/search/semantic")
    public ResponseEntity<List<ContentSearchResponse>> searchSemantic(@RequestBody Map<String, String> requestBody) {
        // 1. 프론트엔드에서 보낸 자연어 쿼리 추출 ㅋㅋㅋㅋ
        String userQuery = requestBody.get("query");

        if (userQuery == null || userQuery.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // 2. 파이썬 사령부(5000번 포트)로 "이 문장 좌표로 바꿔와!"
        String pythonUrl = "http://localhost:5000/vectorize";

        // RestTemplate이 파이썬의 JSON 응답을 double[]로 변환해줍니다!
        double[] vector = restTemplate.postForObject(pythonUrl, Map.of("text", userQuery), double[].class);

        // 3. 받아온 좌표를 DTO에 담아서 서비스 엔진으로 전달
        ContentSearchRequest searchRequest = new ContentSearchRequest();
        searchRequest.setQuery(userQuery);
        searchRequest.setQueryVector(vector);

        // 4. 자바 서비스에서 DB 전수조사 및 유사도 랭킹 산출
        List<ContentSearchResponse> results = semanticSearchService.searchBySemantic(searchRequest);

        // 5. 성공의 리스트 반환
        return ResponseEntity.ok(results);
    }

    @GetMapping("/random")
    public ApiResponse<List<ContentSummaryDto>> getRandomMovies() {
        return ApiResponse.success(contentService.getRandomMovies());
    }

    @GetMapping("/category")
    public ApiResponse<List<ContentSummaryDto>> getCategoryMovies(
            @RequestParam("genreId") Long genreId,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        return ApiResponse.success(contentService.getRandomMoviesByGenre(genreId, limit));
    }

    /**
     * 장르 이름으로 영화 리스트 조회 (프론트엔드 /genre/{genreName} 대응)
     */
    @GetMapping("/genre/{genreName}")
    public ApiResponse<List<ContentSummaryDto>> getMoviesByGenreName(@PathVariable("genreName") String genreName) {
        List<ContentSummaryDto> movies = contentService.getMoviesByGenreName(genreName);
        return ApiResponse.success(movies);
    }

    /**
     * OTT별 영화 리스트 조회 (프론트엔드 /ott/{providerName} 대응)
     */
    @GetMapping("/ott/{providerName}")
    public ApiResponse<List<ContentSummaryDto>> getMoviesByOtt(@PathVariable("providerName") String providerName) {
        List<ContentSummaryDto> movies = contentService.getMoviesByOttName(providerName);
        return ApiResponse.success(movies);
    }

    /**
     * 영화 검색 (SearchService를 사용하여 지독하게 압도적인 하이브리드 검색 수행!)
     */
    @GetMapping("/search")
    public ApiResponse<List<ContentSummaryDto>> searchMovies(@RequestParam("query") String query) {
        System.out.println("DEBUG: 하이브리드 검색 요청 -> " + query);
        return ApiResponse.success(searchService.searchHybrid(query));
    }

    /**
     * 전체 영화 데이터 조회 🚀
     */
    @GetMapping("/all")
    public ApiResponse<List<ContentSummaryDto>> getAllContents() {
        return ApiResponse.success(contentService.getAllMovies());
    }
}