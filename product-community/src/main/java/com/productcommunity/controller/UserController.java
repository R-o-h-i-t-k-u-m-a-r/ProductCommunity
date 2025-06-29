package com.productcommunity.controller;

import com.productcommunity.dto.UserDTO;
import com.productcommunity.exceptions.ResourceNotFoundException;
import com.productcommunity.model.User;
import com.productcommunity.request.UserUpdateRequest;
import com.productcommunity.response.ApiResponse;
import com.productcommunity.service.user.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Tag(name = "User APIs", description = "user related APIs")
@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/users")
@Validated
public class UserController {

    private final IUserService userService;
    private final ModelMapper modelMapper;

    @Operation(summary = "fetch current logged in info")
    @GetMapping("/user")
    public ResponseEntity<ApiResponse> getCurrentUser() {
        try {
            //Fetching authenticated username from SecurityContextHolder of spring security
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse("User not authenticated", null));
            }


            String userName = authentication.getName();

            UserDTO userDto = userService.getByUserName(userName);
            return ResponseEntity.ok(new ApiResponse("Success", userDto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }


    @Operation(summary = "Update user details for current logged in user")
    @PutMapping("/update")
    public ResponseEntity<ApiResponse> updateUser(@Valid @RequestBody UserUpdateRequest request) {
        try {
            //Fetching authenticated username from SecurityContextHolder of spring security
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();

            User user = userService.updateUser(request, userName);
            UserDTO userDto = userService.convertUserToDto(user);
            return ResponseEntity.ok(new ApiResponse("Update User Success!", userDto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @Operation(summary = "Delete account for current logged in user")
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse> deleteUser() {
        try {
            //Fetching authenticated username from SecurityContextHolder of spring security
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();

            userService.deleteUser(userName);
            return ResponseEntity.ok(new ApiResponse("Delete User Success!", null));
        }
        catch (Exception e){
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }

    }

}
