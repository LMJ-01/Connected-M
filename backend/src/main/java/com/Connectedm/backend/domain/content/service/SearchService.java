package com.Connectedm.backend.domain.content.service;

import com.Connectedm.backend.domain.content.dto.ContentSearchRequest;
import com.Connectedm.backend.domain.content.dto.ContentSearchResponse;
import com.Connectedm.backend.domain.content.dto.ContentSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final ContentService contentService;
    private final SemanticSearchService semanticSearchService;
    private final RestTemplate restTemplate;

    @Transactional(readOnly = true)
    public List<ContentSummaryDto> searchHybrid(String query) {
        // 1. 제목 기반 정확도 검색 실행
        List<ContentSummaryDto> titleResults = contentService.searchContents(query);

        // 2. AI 시맨틱 검색을 위한 벡터 변환 (파이썬 서버 호출)
        String pythonUrl = "http://localhost:5000/vectorize";
        double[] vector = restTemplate.postForObject(pythonUrl, Map.of("text", query), double[].class);

        // 3. AI 검색 실행
        ContentSearchRequest searchRequest = new ContentSearchRequest();
        searchRequest.setQuery(query);
        searchRequest.setQueryVector(vector);
        List<ContentSearchResponse> aiResults = semanticSearchService.searchBySemantic(searchRequest);

        // 4. 결과 통합 및 중복 제거
        List<ContentSummaryDto> finalResults = new ArrayList<>(titleResults);

        List<Long> existingIds = titleResults.stream()
                .map(ContentSummaryDto::getId)
                .collect(Collectors.toList());

        for (ContentSearchResponse res : aiResults) {
            if (!existingIds.contains(res.getContentId())) {
                finalResults.add(ContentSummaryDto.builder()
                        .id(res.getContentId())
                        .title(res.getTitle())
                        .posterPath(res.getPosterPath())
                        .overview(res.getOverview())
                        .genres(res.getGenres())
                        .positiveRatio(res.getPositiveRatio())
                        .build());
            }
        }

        return finalResults;
    }
}