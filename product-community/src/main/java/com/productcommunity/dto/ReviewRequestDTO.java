package com.productcommunity.dto;

import com.productcommunity.enums.ReviewRequestStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewRequestDTO {
    private Long id;
    private String productName;
    private String productCode;
    private String productBrand;
    private ReviewRequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserDTO userDTO;
}
