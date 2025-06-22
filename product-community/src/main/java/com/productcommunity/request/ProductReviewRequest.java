package com.productcommunity.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ProductReviewRequest {
    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be less than 255 characters")
    private String title;

    @NotBlank(message = "Content is required")
    @Size(min = 20, max = 400, message = "Content must be between 20-400 characters")
    private String content;


}
