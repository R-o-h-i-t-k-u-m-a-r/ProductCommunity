package com.productcommunity.dto;

import lombok.Data;

@Data
public class UserImageDto {
    private Long id;
    private String fileName;
    private String downloadUrl;
}