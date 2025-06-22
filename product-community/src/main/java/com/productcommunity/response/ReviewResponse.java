package com.productcommunity.response;

import com.productcommunity.enums.ReviewStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewResponse {
    private Long id;
    private Long productId;
    private String productName;
    private Long userId;
    private String userName;
    private String userAvatar;
    private Integer rating;
    private String title;
    private String content;
    private ReviewStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
