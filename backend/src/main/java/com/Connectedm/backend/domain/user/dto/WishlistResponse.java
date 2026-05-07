package com.Connectedm.backend.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WishlistResponse {
    private Long wishlistId;
    private Long contentId;
    private String title;
    private String posterPath;
}
