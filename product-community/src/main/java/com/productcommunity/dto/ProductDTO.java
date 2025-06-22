package com.productcommunity.dto;


import lombok.Data;
import java.util.List;

@Data
public class ProductDTO {
    private Long id;
    private String name;
    private String brand;
    private String code;
    private String description;
    private List<ProductImageDTO> images;
    private List<ReviewDTO> reviews;
}
