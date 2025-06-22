package com.productcommunity.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddProductRequest {
    private Long id;

    @NotBlank(message = "Product name is required")
    private String name;

    @NotBlank(message = "Product code is required")
    private String code;

    @NotBlank(message = "Product brand is required")
    private String brand;

    @NotBlank(message = "Product description is required")
    private String description;
}
