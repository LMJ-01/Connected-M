package com.Connectedm.backend.domain.content.repository;

import com.Connectedm.backend.domain.content.entity.AnalysisCache;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.List;

public interface AnalysisCacheRepository extends JpaRepository<AnalysisCache, Long> {

    // findAll을 오버라이드하여 Content 정보까지 한 번에 쿼리(Join)해서 가져옵니다.
    @Override
    @EntityGraph(attributePaths = {"content"}) // Content 엔티티를 미리 로딩
    List<AnalysisCache> findAll();

    @EntityGraph(attributePaths = {"content"})
    Optional<AnalysisCache> findByContentId(Long contentId);

    List<AnalysisCache> findAllByContentIdIn(List<Long> contentIds);
}