package com.Connectedm.backend.domain.content.service;

import com.Connectedm.backend.domain.content.dto.ContentSearchRequest;
import com.Connectedm.backend.domain.content.dto.ContentSearchResponse;
import com.Connectedm.backend.domain.content.repository.AnalysisCacheRepository;

import com.Connectedm.backend.global.utils.VectorUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SemanticSearchService {
    private final AnalysisCacheRepository analysisCacheRepository;

    public List<ContentSearchResponse> searchBySemantic(ContentSearchRequest request) {
        double[] queryVector = request.getQueryVector();
        String userQuery = getNormalizedQuery(request.getQuery());

        if (queryVector == null || queryVector.length == 0) {
            throw new IllegalArgumentException("검색어의 좌표가 생성되지 않았습니다.");
        }

        return analysisCacheRepository.findAll().stream()
                .filter(cache -> cache.getEmbeddingVector() != null)
                .map(cache -> {
                    double[] targetVector = VectorUtils.parseVectorString(cache.getEmbeddingVector());
                    double score = VectorUtils.cosineSimilarity(queryVector, targetVector);

                    if (cache.getSearchKeywords() != null) {
                        boolean isExactMatch = java.util.Arrays.stream(cache.getSearchKeywords().split(","))
                                .map(String::trim)
                                .anyMatch(keyword -> keyword.equals(userQuery));

                        if (isExactMatch) {
                            score += 10.0;
                        }
                    }

                    boolean isGenreMatch = cache.getContent().getContentGenres().stream()
                            .anyMatch(cg -> {
                                String genreName = cg.getGenre().getName();
                                return genreName.equalsIgnoreCase(userQuery);
                            });

                    if (isGenreMatch) {
                        score += 10.0;
                    }

                    return ContentSearchResponse.builder()
                            .contentId(cache.getContent().getId())
                            .title(cache.getContent().getTitle())
                            .posterPath(cache.getContent().getPosterPath())
                            .similaritySource(score)
                            .overview(cache.getContent().getOverview())
                            .genres(cache.getContent().getContentGenres().stream()
                                    .map(cg -> cg.getGenre().getName())
                                    .collect(Collectors.toList()))
                            .positiveRatio(cache.getPositiveRatio())
                            .build();
                })
                .filter(res -> res.getSimilaritySource() >= 0.45)
                .sorted((a, b) -> Double.compare(b.getSimilaritySource(), a.getSimilaritySource()))
                .limit(10)
                .collect(Collectors.toList());
    }

    private String getNormalizedQuery(String query) {
        if (query == null || query.isBlank()) return "";
        String trimmedQuery = query.trim().toLowerCase();

        return switch (trimmedQuery) {
            // 1. 애니메이션 관련
            case "애니", "만화", "만화영화", "animation" -> "애니메이션";

            // 2. 로맨스/코미디 관련
            case "로코", "롬콤", "romcom" -> "로맨틱 코미디";
            case "멜로", "사랑", "연애", "romance" -> "로맨스";
            case "코믹", "웃긴", "개그", "comedy" -> "코미디";

            // 3. 스릴러/공포 관련
            case "스릴", "슾", "thriller" -> "스릴러";
            case "호러", "무서운", "horror", "심령" -> "공포";
            case "깜놀", "잔인한" -> "공포";

            // 4. SF/판타지 관련
            case "에스에프", "공상과학", "scifi", "sci-fi" -> "sf";
            case "판타", "환상", "fantasy" -> "판타지";
            case "마법", "이세계" -> "판타지";

            // 5. 액션/범죄 관련
            case "액숀", "전투", "싸우는", "action" -> "액션";
            case "히어로", "슈퍼히어로", "hero" -> "액션";
            case "수사", "수사물", "형사", "경찰", "도둑", "crime" -> "범죄";
            case "느와르", "누와르", "암흑가" -> "누와르";

            // 6. 기타 장르 및 한국적 표현
            case "다큐", "실화", "documentary" -> "다큐멘터리";
            case "역사", "시대극", "고전", "임금" -> "사극";
            case "가족물", "애들", "family" -> "가족";
            case "전쟁물", "war" -> "전쟁";
            case "전투기", "스텔스" -> "전투기";
            case "귀신", "좀비" -> "공포";

            case "드라마", "drama" -> "드라마";
            case "미스터리", "mystery" -> "미스터리";

            default -> query;
        };
    }
}