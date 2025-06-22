package com.productcommunity.controller;

import com.productcommunity.dto.UserImageDto;
import com.productcommunity.exceptions.AlreadyExistsException;
import com.productcommunity.exceptions.ResourceNotFoundException;
import com.productcommunity.model.UserImage;
import com.productcommunity.response.ApiResponse;
import com.productcommunity.service.UserImage.IUserImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.sql.SQLException;

import static org.springframework.http.HttpStatus.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/user")
@Tag(name = "User Image APIs", description = "user image related APIs")
public class UserImageController {

    private final IUserImageService imageService;

    @Operation(summary = "add user image")
    //@PreAuthorize("hasAnyRole('NORMAL', 'ADMIN')")
    @PreAuthorize("hasRole('NORMAL') or hasRole('ADMIN')")
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse> saveImages(@RequestParam MultipartFile file, @RequestParam Long userId) {
        try {
            UserImageDto imageDto = imageService.saveImage(userId, file);
            return ResponseEntity.ok(new ApiResponse("Upload success!", imageDto));
        } catch (AlreadyExistsException e){
            return ResponseEntity.status(CONFLICT).body(new ApiResponse("Upload failed!", e.getMessage()));
        }
        catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("Upload failed!", e.getMessage()));
        }

    }

    @Operation(summary = "fetch the user image")
    @GetMapping("/image/download/{imageId}")
    public ResponseEntity<Resource> downloadImage(@PathVariable @NotNull Long imageId) throws SQLException {
        UserImage image = imageService.getImageById(imageId);
        ByteArrayResource resource = new ByteArrayResource(image.getImage().getBytes(1, (int) image.getImage().length()));
        return  ResponseEntity.ok().contentType(MediaType.parseMediaType(image.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" +image.getFileName() + "\"")
                .body(resource);
    }

    @Operation(summary = "update user image")
    @PreAuthorize("hasAnyRole('NORMAL', 'ADMIN')")
    @PutMapping("/image/{imageId}/update")
    public ResponseEntity<ApiResponse> updateImage(@PathVariable Long imageId, @RequestBody MultipartFile file) {
        try {
            UserImage image = imageService.getImageById(imageId);
            if(image != null) {
                imageService.updateImage(file, imageId);
                return ResponseEntity.ok(new ApiResponse("Update success!", null));
            }
        } catch (ResourceNotFoundException e) {
            return  ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("Update failed!", INTERNAL_SERVER_ERROR));
    }

    @Operation(summary = "delete user image")
    @PreAuthorize("hasAnyRole('NORMAL', 'ADMIN')")
    @DeleteMapping("/image/{imageId}/delete")
    public ResponseEntity<ApiResponse> deleteImage(@PathVariable @NotNull Long imageId) {
        try {
            UserImage image = imageService.getImageById(imageId);
            if(image != null) {
                imageService.deleteImageById( imageId);
                return ResponseEntity.ok(new ApiResponse("Delete success!", null));
            }
        } catch (ResourceNotFoundException e) {
            return  ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("Delete failed!", INTERNAL_SERVER_ERROR));
    }
}
