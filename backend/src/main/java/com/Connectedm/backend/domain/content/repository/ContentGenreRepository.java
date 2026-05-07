package com.Connectedm.backend.domain.content.repository;

import com.Connectedm.backend.domain.content.entity.Content;
import com.Connectedm.backend.domain.content.entity.ContentGenre;
import com.Connectedm.backend.domain.content.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ContentGenreRepository extends JpaRepository<ContentGenre, Long> {
    List<ContentGenre> findByGenre(Genre genre);

    List<ContentGenre> findAllByContentIdIn(List<Long> contentIds);
}
