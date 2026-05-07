package com.Connectedm.backend.domain.content.repository;

import com.Connectedm.backend.domain.content.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ContentRepository extends JpaRepository<Content, Long> {


    // TMDB ID로 영화 찾는 기능
    Optional<Content> findByTmdbId(String tmdbId);

    // 영화 상세 조회 시 AI분석캐시를 한번에 가져오는 기능
    @Query("SELECT c From Content c LEFT JOIN FETCH c.analysisCache WHERE c.id = :id")
    Optional<Content> findWithCacheById(@Param("id") Long id);


    // 오늘의 추천작 : 최신등록순 10개
    List<Content> findTop10ByOrderByIdDesc();

    // 카테고리별 영화 : 장르 ID로 필터링
    @Query("SELECT c FROM Content c " +
            "JOIN c.contentGenres cg " +
            "WHERE cg.genre.id = :genreId")
    List<Content> findByGenreId(@Param("genreId") Long genreId);


    // 1. 오늘의 추천작용: 우리 DB 전체에서 랜덤하게 뽑기
    @Query(value = "SELECT * FROM content ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<Content> findRandomContents(@Param("limit") int limit);

    // 2. 카테고리 탭용: 특정 장르 내에서 랜덤하게 뽑기
    @Query(value = "SELECT c.* FROM content c " +
            "JOIN content_genre cg ON c.id = cg.content_id " +
            "WHERE cg.genre_id = :genreId " +
            "ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<Content> findRandomByGenreId(@Param("genreId") Long genreId, @Param("limit") int limit);

    // 특정 OTT 로고 해시값을 포함하는 영화 찾기
    @Query("SELECT c FROM Content c WHERE c.ottLogos LIKE %:logoHash%")
    List<Content> findByOttLogosContaining(@Param("logoHash") String logoHash);

    // 영화 제목으로 검색 (LIKE %query% 효과)
    List<Content> findByTitleContaining(String title);

    /**
     * AI 벡터 유사도 기반 검색 (Native Query)
     * 사용자가 입력한 검색어의 벡터와 가장 거리가 가까운 영화를 순서대로 가져옵니다.
     */
    @Query(value = "SELECT c.* FROM content c " +
            "JOIN analysis_cache ac ON c.id = ac.content_id " +
            "ORDER BY (ac.embedding_vector <=> CAST(:searchVector AS BINARY)) ASC " +
            "LIMIT :limit", nativeQuery = true)
    List<Content> findBySemanticSearch(@Param("searchVector") String searchVector, @Param("limit") int limit);

    /**
     * 특정 영화와 가장 유사한 분위기의 영화 추천 (Native Query)
     * 🚀 현재 영화의 벡터(embedding_vector)를 기준으로 다른 영화들과의 거리를 계산합니다.
     * 1. 현재 보고 있는 영화(contentId)는 추천에서 제외합니다.
     * 2. 서브쿼리를 통해 대상 영화의 벡터를 가져와 다른 영화들과 비교 연산(<=>)을 수행합니다.
     */
    @Query(value = "SELECT c.* FROM content c " +
            "JOIN analysis_cache ac ON c.id = ac.content_id " +
            "WHERE c.id != :contentId " +
            "ORDER BY (ac.embedding_vector <=> (SELECT embedding_vector FROM analysis_cache WHERE content_id = :contentId)) ASC " +
            "LIMIT :limit", nativeQuery = true)
    List<Content> findSimilarContentByVector(@Param("contentId") Long contentId, @Param("limit") int limit);
}
