package com.productcommunity.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProductInfoDto {
    private Long id;
    private String name;
    private String brand;
    private String code;
    private String description;
    private Float avgRating;
    private Integer totalReveiws;
    private List<ProductImageDTO> images;
}
