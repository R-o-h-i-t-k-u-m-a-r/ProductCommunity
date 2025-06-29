package com.productcommunity.request;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateReviewRequest {
    @NotNull(message = "Product ID is required")
    private String productName;

    @NotNull(message = "Product ID is required")
    private String productCode;

    @NotNull(message = "Product ID is required")
    private String productBrand;
}
