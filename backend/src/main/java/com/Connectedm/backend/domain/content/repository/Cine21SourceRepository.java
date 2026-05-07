package com.Connectedm.backend.domain.content.repository;

import com.Connectedm.backend.domain.content.entity.Cine21Source;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface Cine21SourceRepository extends JpaRepository<Cine21Source,Long> {

    // 씨네21 고유 영화 ID로 소스 찾기
    Optional<Cine21Source> findByExternalMovieId(String externalMovieId);

    // 원본 제목으로 찾고 싶을 때
    Optional<Cine21Source> findByRawTitle(String rawTitle);
}
