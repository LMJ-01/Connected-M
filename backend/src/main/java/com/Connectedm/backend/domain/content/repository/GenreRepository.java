package com.Connectedm.backend.domain.content.repository;

import com.Connectedm.backend.domain.content.entity.Content;
import com.Connectedm.backend.domain.content.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GenreRepository extends JpaRepository<Genre, Long> {
    // 장르 이름으로 찾는 기능 (예: "ACTION")
    Optional<Genre> findByName(String name);
}
