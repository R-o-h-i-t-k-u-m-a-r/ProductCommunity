package com.productcommunity.dto;

import com.productcommunity.enums.ReviewStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewDTO {
    private Long id;
    private Integer rating;
    private String title;
    private String content;
    private ReviewStatus reviewStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserDTO userDTO;
}
