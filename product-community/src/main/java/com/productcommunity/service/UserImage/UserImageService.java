package com.productcommunity.service.UserImage;

import com.productcommunity.dto.UserImageDto;
import com.productcommunity.exceptions.AlreadyExistsException;
import com.productcommunity.exceptions.ResourceNotFoundException;
import com.productcommunity.model.User;
import com.productcommunity.model.UserImage;
import com.productcommunity.repository.UserImageRepository;
import com.productcommunity.repository.UserRepository;
import com.productcommunity.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.sql.SQLException;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserImageService implements IUserImageService{

    private final UserImageRepository imageRepository;
    private final IUserService userService;
    private final UserRepository userRepository;

    /**
     * @param id
     * @return
     */
    @Override
    public UserImage getImageById(Long id) {
        return imageRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("No image found with id: " + id));
    }

    /**
     * @param id
     */
    @Override
    @Transactional
    public void deleteImageById(Long id) {

        UserImage userImage = imageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No image found with id: " + id));

        // Break bi-directional reference
        User user = userImage.getUser();
        if (user != null) {
            user.setUserImage(null);
            userRepository.save(user);
        }

        imageRepository.delete(userImage);
        log.info("Deleted image with id: {}", id);
    }

    /**
     * @param userId
     * @param file
     * @return
     */
    @Override
    public UserImageDto saveImage(Long userId, MultipartFile file) {
        User user = userService.getUserById(userId);

        // Check if user already has an image
        if (user.getUserImage() != null) {
            throw new AlreadyExistsException("Image already exists for this user. Please update the image instead.");
        }

        UserImageDto imageDto = new UserImageDto();
        try {
            UserImage image = new UserImage();
            image.setFileName(file.getOriginalFilename());
            image.setFileType(file.getContentType());
            image.setImage(new SerialBlob(file.getBytes()));
            image.setUser(user);

            String buildDownloadUrl = "/api/v1/user/image/download/";
            String downloadUrl = buildDownloadUrl+image.getId();
            image.setDownloadUrl(downloadUrl);
            UserImage savedImage = imageRepository.save(image);

            savedImage.setDownloadUrl(buildDownloadUrl+savedImage.getId());
            imageRepository.save(savedImage);


            imageDto.setId(savedImage.getId());
            imageDto.setFileName(savedImage.getFileName());
            imageDto.setDownloadUrl(savedImage.getDownloadUrl());

        } catch (SQLException e) {
            throw new AlreadyExistsException("Image already exists for this user please try to update the image :");
        } catch (IOException e){
            throw new RuntimeException(e.getMessage());
        }
        return imageDto;
    }

    /**
     * @param file
     * @param imageId
     */
    @Override
    public void updateImage(MultipartFile file, Long imageId) {
        UserImage image = getImageById(imageId);
        try {
            image.setFileName(file.getOriginalFilename());
            image.setFileType(file.getContentType());
            image.setImage(new SerialBlob(file.getBytes()));
            imageRepository.save(image);
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e.getMessage());
        }

    }
}
