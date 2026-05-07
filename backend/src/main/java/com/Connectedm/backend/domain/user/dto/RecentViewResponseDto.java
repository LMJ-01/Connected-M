package com.Connectedm.backend.domain.user.dto;

import com.Connectedm.backend.domain.user.entity.RecentView;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
public class RecentViewResponseDto {
    private final Long id;
    private final Long contentId;
    private final String title;
    private final String posterPath;
    private final String viewedAt;
    private final String detailPath;    // 프론트 이동 경로


    public RecentViewResponseDto(RecentView recentView) {
        this.id = recentView.getId();
        this.contentId = recentView.getContent().getId();
        this.title = recentView.getContent().getTitle();
        this.posterPath = recentView.getContent().getPosterPath();
        this.viewedAt = recentView.getViewedAt()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        this.detailPath = "/movie-detail/" + recentView.getContent().getId(); // 프론트 배관 연결 ㅋ
    }
}
