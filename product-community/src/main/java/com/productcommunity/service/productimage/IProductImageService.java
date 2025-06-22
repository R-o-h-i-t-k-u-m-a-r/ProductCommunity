package com.productcommunity.service.productimage;

import com.productcommunity.dto.ProductImageDTO;
import com.productcommunity.model.ProductImage;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IProductImageService {
    ProductImage getImageById(Long id);
    void deleteImageById(Long id);
    List<ProductImageDTO> saveImages(Long productId, List<MultipartFile> files);
}
