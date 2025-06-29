package com.productcommunity.service.productimage;

import com.productcommunity.repository.ProductImageRepository;
import com.productcommunity.dto.ProductImageDTO;
import com.productcommunity.exceptions.ResourceNotFoundException;
import com.productcommunity.model.Product;
import com.productcommunity.model.ProductImage;
import com.productcommunity.service.product.IProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductImageService implements IProductImageService{
    private final ProductImageRepository imageRepository;
    private final IProductService productService;

    @Override
    public ProductImage getImageById(Long id) {
        return imageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No image found with id: " + id));
    }

    @Override
    public void deleteImageById(Long id) {
        imageRepository.findById(id).ifPresentOrElse(imageRepository::delete, () -> {
            throw new ResourceNotFoundException("No image found with id: " + id);
        });
    }

//    @Override
//    @Transactional
//    public List<ProductImageDTO> saveImages(Long productId, List<MultipartFile> files) {
//        Product product = productService.getProductById(productId);
//
//        List<ProductImageDTO> savedImageDto = new ArrayList<>();
//        for (MultipartFile file : files) {
//            try {
//                ProductImage image = new ProductImage();
//                image.setFileName(file.getOriginalFilename());
//                image.setFileType(file.getContentType());
//                image.setImage(new SerialBlob(file.getBytes()));
//                image.setProduct(product);
//
//                // Save image first to get ID
//                ProductImage savedImage = imageRepository.save(image);
//
//                // Now that ID is available, build and set download URL
//                String downloadUrl = "/api/v1/images/image/download/" + savedImage.getId();
//                savedImage.setDownloadUrl(downloadUrl);
//
//                // Save again with updated download URL
//                savedImage = imageRepository.save(savedImage);
//
//                // Convert to DTO
//                ProductImageDTO imageDto = new ProductImageDTO();
//                imageDto.setId(savedImage.getId());
//                imageDto.setFileName(savedImage.getFileName());
//                imageDto.setDownloadUrl(savedImage.getDownloadUrl());
//                savedImageDto.add(imageDto);
//
//            }   catch(IOException | SQLException e){
//                e.printStackTrace();
//                throw new RuntimeException(e.getMessage());
//            }
//        }
//        return savedImageDto;
//    }



    @Override
    @Transactional
    public List<ProductImageDTO> saveImages(Long productId, List<MultipartFile> files) {
        Product product = productService.getProductById(productId);

        // Create a new list to avoid modifying the persistent collection directly
        List<ProductImage> newImages = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                ProductImage image = new ProductImage();
                image.setFileName(file.getOriginalFilename());
                image.setFileType(file.getContentType());
                image.setImage(new SerialBlob(file.getBytes()));
                image.setProduct(product);
                newImages.add(image);
            } catch(IOException | SQLException e) {
                throw new RuntimeException("Failed to process image: " + e.getMessage());
            }
        }

        // Save all images at once
        List<ProductImage> savedImages = imageRepository.saveAll(newImages);

        // Update download URLs
        List<ProductImage> updatedImages = savedImages.stream()
                .map(image -> {
                    String downloadUrl = "/api/v1/images/image/download/" + image.getId();
                    image.setDownloadUrl(downloadUrl);
                    return image;
                })
                .collect(Collectors.toList());

        // Save updated images
        imageRepository.saveAll(updatedImages);

        // Convert to DTOs
        return updatedImages.stream()
                .map(image -> {
                    ProductImageDTO dto = new ProductImageDTO();
                    dto.setId(image.getId());
                    dto.setFileName(image.getFileName());
                    dto.setDownloadUrl(image.getDownloadUrl());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
