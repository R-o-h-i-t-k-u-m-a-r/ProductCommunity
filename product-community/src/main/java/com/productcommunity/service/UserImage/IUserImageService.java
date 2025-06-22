package com.productcommunity.service.UserImage;

import com.productcommunity.dto.UserImageDto;
import com.productcommunity.model.UserImage;
import org.springframework.web.multipart.MultipartFile;

public interface IUserImageService {
    UserImage getImageById(Long id);
    void deleteImageById(Long id);
    UserImageDto saveImage(Long userId, MultipartFile file);
    void updateImage(MultipartFile file, Long imageId);
}

