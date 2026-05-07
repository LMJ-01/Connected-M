package com.Connectedm.backend.domain.content.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.Connectedm.backend.domain.content.dto.*;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Connectedm.backend.domain.content.entity.Content;
import com.Connectedm.backend.domain.content.entity.ContentGenre;
import com.Connectedm.backend.domain.content.entity.Genre;
import com.Connectedm.backend.domain.content.repository.AnalysisCacheRepository;
import com.Connectedm.backend.domain.content.repository.ContentGenreRepository;
import com.Connectedm.backend.domain.content.repository.ContentRepository;
import com.Connectedm.backend.domain.content.repository.GenreRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ContentService {

    private final ContentRepository contentRepository;
    private final GenreRepository genreRepository;
    private final AnalysisCacheRepository analysisCacheRepository;
    private final ContentGenreRepository contentGenreRepository;
    private final ReviewService reviewService;
    private final TmdbService tmdbService;

    /**
     * TMDB 상세 정보를 강제로 업데이트하고 장르를 동기화함
     */
    @Transactional
    public void updateContentWithTmdb(Long id) {
        // 1. DB에서 해당 콘텐츠 조회
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("콘텐츠를 찾을 수 없습니다. ID: " + id));

        // 2. TMDB ID 결정 (기존 ID 우선, 없으면 제목 검색)
        String tmdbIdToUse = content.getTmdbId();
        if (tmdbIdToUse == null || tmdbIdToUse.isBlank()) {
            Long searchedId = tmdbService.searchMovieIdByTitle(content.getTitle().trim());
            if (searchedId == null) {
                throw new RuntimeException("TMDB에서 해당 영화 제목을 찾을 수 없습니다: " + content.getTitle());
            }
            tmdbIdToUse = String.valueOf(searchedId);
        }

        // 3. TMDB API 호출
        TmdbMovieResponseDto tmdbData = tmdbService.getMovieDetail(tmdbIdToUse);
        if (tmdbData == null) {
            throw new RuntimeException("TMDB 상세 정보를 불러올 수 없습니다. ID: " + tmdbIdToUse);
        }

        // 4. 이미지 경로 생성 (포스터: w500, 배경: w1280)
        String fullPosterPath = (tmdbData.getPoster_path() != null)
                ? "https://image.tmdb.org/t/p/w500" + tmdbData.getPoster_path()
                : content.getPosterPath();

        String fullBackdropPath = (tmdbData.getBackdrop_path() != null)
                ? "https://image.tmdb.org/t/p/w1280" + tmdbData.getBackdrop_path()
                : content.getBackdropPath();

        // 5. OTT 로고 데이터 준비
        String logos = (tmdbData.getOttLogos() != null) ? tmdbData.getOttLogos() : "";

        // 6. 장르 동기화 로직
        if (tmdbData.getGenres() != null) {
            for (TmdbMovieResponseDto.TmdbGenre tmdbGenre : tmdbData.getGenres()) {
                String genreName = tmdbGenre.getName();

                Genre genre = genreRepository.findByName(genreName)
                        .orElseGet(() -> genreRepository.save(Genre.builder().name(genreName).build()));

                boolean alreadyLinked = content.getContentGenres().stream()
                        .anyMatch(cg -> cg.getGenre().getName().equals(genreName));

                if (!alreadyLinked) {
                    ContentGenre contentGenre = ContentGenre.builder()
                            .content(content)
                            .genre(genre)
                            .build();
                    content.addGenre(contentGenre);
                }
            }
        }

        // 7. 통합 업데이트 메서드 호출 (Content.java에서 수정한 FullInfo 메서드)
        content.updateFullInfo(
                tmdbIdToUse,
                tmdbData.getOverview(),
                fullPosterPath,
                fullBackdropPath,
                logos,
                tmdbData.getRuntime(),
                tmdbData.getAgeRating()
        );

        contentRepository.save(content);
    }

    /**
     * 콘텐츠 상세 조회 및 누락된 데이터 자동 보완
     */
    /**
     * 콘텐츠 상세 조회 및 누락된 데이터 자동 보완
     */
    @Transactional
    public ContentDetailResponseDto getContentDetail(Long id) {
        Content content = contentRepository.findWithCacheById(id)
                .orElseThrow(() -> new RuntimeException("해당 콘텐츠를 찾을 수 없습니다. ID: " + id));

        TmdbMovieResponseDto tmdbData = tmdbService.getMovieDetail(content.getTmdbId());

        // 데이터 업데이트 조건 확인 (줄거리, 포스터, 또는 배경 이미지가 비어있는 경우)
        boolean isOverviewEmpty = content.getOverview() == null || content.getOverview().isBlank();
        boolean isPosterBroken = content.getPosterPath() == null || content.getPosterPath().endsWith("/w500/");
        boolean isBackdropEmpty = content.getBackdropPath() == null || content.getBackdropPath().isBlank();
        boolean isExtraInfoEmpty = content.getRuntime() == null || content.getAgeRating() == null;

        if ((isOverviewEmpty || isPosterBroken || isBackdropEmpty || isExtraInfoEmpty) && tmdbData != null) {
            String realPosterPath = (tmdbData.getPoster_path() != null)
                    ? "https://image.tmdb.org/t/p/w500" + tmdbData.getPoster_path()
                    : content.getPosterPath();

            String realBackdropPath = (tmdbData.getBackdrop_path() != null)
                    ? "https://image.tmdb.org/t/p/w1280" + tmdbData.getBackdrop_path()
                    : content.getBackdropPath();

            String logos = tmdbData.getOttLogos() != null ? tmdbData.getOttLogos() : "";

            // DB 업데이트
            content.updateFullInfo(content.getTmdbId(), tmdbData.getOverview(), realPosterPath, realBackdropPath, logos, tmdbData.getRuntime(), tmdbData.getAgeRating());
            contentRepository.saveAndFlush(content);
        }

        // DTO 조립용 변수 준비
        List<String> genreNames = content.getContentGenres().stream()
                .map(cg -> cg.getGenre().getName())
                .collect(Collectors.toList());

        String summary = (content.getAnalysisCache() != null) ? content.getAnalysisCache().getSummary() : "분석 중입니다.";
        Double ratio = (content.getAnalysisCache() != null) ? content.getAnalysisCache().getPositiveRatio() : 0.0;
        List<String> keywords = Collections.emptyList();
        if (content.getAnalysisCache() != null && content.getAnalysisCache().getTopKeywords() != null) {
            keywords = java.util.Arrays.stream(content.getAnalysisCache().getTopKeywords().split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
        }

        List<ReviewResponseDto> expertReviews = reviewService.getExpertReviews(id);
        List<UserReviewResponseDto> userReviews = reviewService.getUserReviews(id);
        List<ContentSummaryDto> recommendations = getSemanticRecommendations(id);
        ReviewStatsResponseDto stats = reviewService.getReviewStats(id);

        List<TmdbMovieResponseDto.TmdbCastItem> majorCasts = Collections.emptyList();
        if (tmdbData != null && tmdbData.getCredits() != null && tmdbData.getCredits().getCast() != null) {
            majorCasts = tmdbData.getCredits().getCast().stream()
                    .filter(cast -> cast.getOrder() < 10)
                    .limit(8)
                    .collect(Collectors.toList());
        }

        // 최종 DTO 빌드
        return ContentDetailResponseDto.builder()
                .id(content.getId())
                .title(content.getTitle())
                .overview(content.getOverview())
                .posterPath(content.getPosterPath())
                .backdropPath(content.getBackdropPath())
                .castList(majorCasts)
                .ottLogos(content.getOttLogos())
                .genres(genreNames)
                .aiSummary(summary)
                .positiveRatio(ratio)
                .topKeywords(keywords)
                .expertReviews(expertReviews)
                .userReviews(userReviews)
                .userRatingAvg(stats.getUserRatingAvg())
                .expertRatingAvg(stats.getExpertRatingAvg())
                .expertReviewCount(stats.getExpertReviewCount())
                .runtime(content.getRuntime())
                .ageRating(content.getAgeRating())
                .trailerKey(tmdbData != null ? tmdbData.getTrailerKey() : null)
                .recommendations(recommendations)
                .build();
    }

    // --- 이하 기존 메인페이지 및 저장 로직 (유지) ---

    public void saveCrawledContent(ContentCreateRequestDto dto) {
        if (contentRepository.findByTmdbId(dto.getTmdbId()).isPresent()) return;
        Content content = contentRepository.save(dto.toEntity());
        for (String genreName : dto.getGenres()) {
            Genre genre = genreRepository.findByName(genreName)
                    .orElseThrow(() -> new RuntimeException("장르 미정 : " + genreName));
            ContentGenre cg = ContentGenre.builder()
                    .content(content)
                    .genre(genre)
                    .build();
            content.addGenre(cg);
        }
    }

    @Transactional(readOnly = true)
    public MainPageResponseDto getMainPageData() {
        List<Content> allContents = contentRepository.findAll();
        String backgroundImage = "";
        if (!allContents.isEmpty()) {
            Collections.shuffle(allContents);
            backgroundImage = allContents.get(0).getPosterPath();
        }

        List<ContentSummaryDto> todayRecommendations = analysisCacheRepository.findAll(
                        Sort.by(Sort.Direction.DESC, "positiveRatio")
                ).stream()
                .limit(10)
                .map(cache -> ContentSummaryDto.builder()
                        .id(cache.getContent().getId())
                        .title(cache.getContent().getTitle())
                        .posterPath(cache.getContent().getPosterPath())
                        .positiveRatio(cache.getPositiveRatio())
                        .build())
                .collect(Collectors.toList());

        List<Genre> genres = genreRepository.findAll();
        Map<String, List<ContentSummaryDto>> genreContents = new HashMap<>();
        for (Genre genre : genres) {
            List<ContentSummaryDto> contents = contentGenreRepository.findByGenre(genre).stream()
                    .limit(15)
                    .map(cg -> {
                        Content content = cg.getContent();
                        Double ratio = (content.getAnalysisCache() != null) ?
                                content.getAnalysisCache().getPositiveRatio() : 0.0;
                        return ContentSummaryDto.builder()
                                .id(content.getId())
                                .title(content.getTitle())
                                .posterPath(content.getPosterPath())
                                .positiveRatio(ratio)
                                .build();
                    })
                    .collect(Collectors.toList());
            genreContents.put(genre.getName(), contents);
        }
        return MainPageResponseDto.builder()
                .backgroundImage(backgroundImage)
                .todayRecommendations(todayRecommendations)
                .genreContents(genreContents)
                .build();
    }

    @Transactional(readOnly = true)
    public Content findById(Long contentId) {
        return contentRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 콘텐츠입니다. ID: " + contentId));
    }

    @Transactional(readOnly = true)
    public List<ContentSummaryDto> getRandomMovies() {
        return contentRepository.findRandomContents(5).stream()
                .map(c -> ContentSummaryDto.builder()
                        .id(c.getId())
                        .title(c.getTitle())
                        .posterPath(c.getPosterPath())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ContentSummaryDto> getRandomMoviesByGenre(Long genreId, int limit) {
        return contentRepository.findRandomByGenreId(genreId, limit).stream()
                .map(c -> ContentSummaryDto.builder()
                        .id(c.getId())
                        .title(c.getTitle())
                        .posterPath(c.getPosterPath())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 장르 이름(String)으로 무작위 영화 리스트를 조회하는 메서드 (새로 추가됨)
     */
    @Transactional(readOnly = true)
    public List<ContentSummaryDto> getMoviesByGenreName(String genreName) {
        // 1. 장르 이름으로 DB에서 진짜 Genre 객체를 찾습니다.
        Genre genre = genreRepository.findByName(genreName)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 장르입니다: " + genreName));

        // 2. 찾은 장르 ID를 이용해 기존에 만들어두신 getRandomMoviesByGenre를 재사용합니다! (한 번에 20개 정도 보내주도록 설정)
        return getRandomMoviesByGenre(genre.getId(), 20);
    }


    /**
     * 프론트엔드에서 넘어온 OTT 이름(netflix 등)으로 영화 리스트 조회
     */
    @Transactional(readOnly = true)
    public List<ContentSummaryDto> getMoviesByOttName(String providerName) {
        String logoHash = "";

        // 프론트에서 보낸 이름에 맞춰 명준님이 저장해둔 고유 해시값 매핑
        switch(providerName.toLowerCase()) {
            case "netflix": logoHash = "pbpMk2JmcoNnQwx5JGpXngfoWtp"; break; // 넷플릭스는 해시가 2개지만 대표 하나로 검색
            case "disney": logoHash = "97yvRBw1GzX7fXprcF80er19ot"; break;
            case "tving": logoHash = "qHThQdkJuROK0k5QTCrknaNukWe"; break;
            case "wavve": logoHash = "hPcjSaWfMwEqXaCMu7Fkb529Dkc"; break;
            case "watcha": logoHash = "5gmEivxOGPdq4Afpq1f8ktLtEW1"; break;
            case "coupang": logoHash = "5gmEivxOGPdqQ0A09uXp9Gf1vSj"; break;
            default: return Collections.emptyList(); // 모르는 OTT면 빈 리스트 반환
        }

        // 해당 해시값을 포함하는 영화 30개를 뽑아서 DTO로 변환
        return contentRepository.findByOttLogosContaining(logoHash).stream()
                .limit(30)
                .map(c -> ContentSummaryDto.builder()
                        .id(c.getId())
                        .title(c.getTitle())
                        .posterPath(c.getPosterPath())
                        .build())
                .collect(Collectors.toList());
    }

    // [신규 추가] 영화 제목으로 검색 (LIKE %query% 검색)
    @Transactional(readOnly = true)
    public List<ContentSummaryDto> searchContents(String query) {
        // 검색어가 비어있으면 빈 리스트 반환
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return contentRepository.findByTitleContaining(query).stream()
                .map(c -> ContentSummaryDto.builder()
                        .id(c.getId())
                        .title(c.getTitle())
                        .posterPath(c.getPosterPath())
                        // 필요하다면 여기서 releaseDate나 overview도 추가 가능
                        .overview((c.getOverview() != null && !c.getOverview().isBlank())
                                ? c.getOverview()
                                : (c.getAnalysisCache() != null ? c.getAnalysisCache().getSummary() : "정보가 없습니다."))
                        .genres(c.getContentGenres().stream()
                                .map(cg -> cg.getGenre().getName())
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * [신규 추가] AI 시맨틱 검색: 검색어의 의미(Vector)를 분석하여 가장 유사한 영화 추천
     * 영화 제목이 기억나지 않아도 "폭력", "잔인한", "범죄" 등 키워드나 문장으로 검색 가능
     */
    @Transactional(readOnly = true)
    public List<ContentSummaryDto> searchByAi(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        try {
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            String pythonUrl = "http://localhost:5000/vector";

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("text", query);

            Map<String, Object> response = restTemplate.postForObject(pythonUrl, requestBody, Map.class);
            List<Double> vector = (List<Double>) response.get("vector");

            List<Content> contents = contentRepository.findBySemanticSearch(vector.toString(), 15);

            List<Long> contentIds = contents.stream()
                    .map(Content::getId)
                    .collect(Collectors.toList());

            Map<Long, com.Connectedm.backend.domain.content.entity.AnalysisCache> cacheMap = analysisCacheRepository.findAllByContentIdIn(contentIds).stream()
                    .collect(Collectors.toMap(
                            ac -> ac.getContent().getId(),
                            ac -> ac,
                            (v1, v2) -> v1
                    ));

            Map<Long, List<String>> genreMap = contentGenreRepository.findAllByContentIdIn(contentIds).stream()
                    .collect(Collectors.groupingBy(
                            cg -> cg.getContent().getId(),
                            Collectors.mapping(cg -> cg.getGenre().getName(), Collectors.toList())
                    ));

            return contents.stream()
                    .map(c -> {
                        var cache = cacheMap.get(c.getId());
                        List<String> movieGenres = genreMap.getOrDefault(c.getId(), Collections.emptyList());

                        return ContentSummaryDto.builder()
                                .id(c.getId())
                                .title(c.getTitle())
                                .posterPath(c.getPosterPath())
                                .overview((c.getOverview() != null && !c.getOverview().isBlank())
                                        ? c.getOverview()
                                        : (cache != null ? cache.getSummary() : "상세 정보 준비 중입니다."))
                                .genres(movieGenres)
                                .positiveRatio(cache != null ? cache.getPositiveRatio() : 0.0)
                                .build();
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("❌ [AI 검색 에러] 일반 제목 검색으로 전환합니다: " + e.getMessage());
            return searchContents(query);
        }
    }

    /**
     * 전체 영화 리스트 조회
     * KeywordPage에서 211개 영화를 모두 보여주기 위해 사용됩니다.
     */
    @Transactional(readOnly = true)
    public List<ContentSummaryDto> getAllMovies() {
        // 1. DB에 있는 모든 영화(Content)를 긁어옵니다.
        return contentRepository.findAll().stream()
                .map(content -> {
                    // 2. 각 영화의 AI 분석 점수(positiveRatio)를 챙깁니다.
                    Double ratio = (content.getAnalysisCache() != null)
                            ? content.getAnalysisCache().getPositiveRatio()
                            : 0.0;

                    // 3. 프론트엔드 KeywordPage가 원하는 깔끔한 바구니(DTO)에 담습니다.
                    return ContentSummaryDto.builder()
                            .id(content.getId())
                            .title(content.getTitle())
                            .posterPath(content.getPosterPath())
                            .positiveRatio(ratio)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 파이썬 FastAPI 서버에 벡터 매칭을 요청하여 진짜 유사한 영화 딱 3개를 가져옵니다.
     */
    @Transactional(readOnly = true)
    public List<ContentSummaryDto> getSemanticRecommendations(Long contentId) {
        try {
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            String pythonUrl = "http://localhost:5000/recommend";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("content_id", contentId);
            requestBody.put("limit", 3);

            Map<String, Object> response = restTemplate.postForObject(pythonUrl, requestBody, Map.class);
            List<Map<String, Object>> recs = (List<Map<String, Object>>) response.get("recommendations");

            List<ContentSummaryDto> result = new java.util.ArrayList<>();
            for (Map<String, Object> rec : recs) {
                Long id = Long.valueOf(rec.get("id").toString());
                contentRepository.findById(id).ifPresent(c -> {
                    Double ratio = (c.getAnalysisCache() != null) ? c.getAnalysisCache().getPositiveRatio() : 0.0;

                    if (ratio > 1.0) {
                        ratio = ratio / 100.0;
                    }

                    result.add(ContentSummaryDto.builder()
                            .id(c.getId())
                            .title(c.getTitle())
                            .posterPath(c.getPosterPath())
                            .positiveRatio(ratio)
                            .build());
                });
            }
            return result;

        } catch (Exception e) {
            System.err.println("❌ [AI 추천 에러] 파이썬 서버 연결 실패. 같은 장르로 3개 대체합니다: " + e.getMessage());
            Content content = contentRepository.findById(contentId).orElse(null);
            if (content != null && !content.getContentGenres().isEmpty()) {
                Long genreId = content.getContentGenres().get(0).getGenre().getId();
                return getRandomMoviesByGenre(genreId, 3);
            }
            return Collections.emptyList();
        }
    }
}