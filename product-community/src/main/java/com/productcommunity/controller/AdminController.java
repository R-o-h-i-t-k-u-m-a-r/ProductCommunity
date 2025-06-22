package com.productcommunity.controller;

import com.productcommunity.dto.CreateUserDto;
import com.productcommunity.dto.UserDTO;
import com.productcommunity.model.User;
import com.productcommunity.request.CreateUserRequest;
import com.productcommunity.response.ApiResponse;
import com.productcommunity.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.CONFLICT;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/admin")
public class AdminController {

    private final IUserService userService;
    private final AuthenticationManager authenticationManager;
    private final ModelMapper modelMapper;

    @GetMapping("/all-user")
    public ResponseEntity<ApiResponse> getAllUsers(){
        List<UserDTO> allUser = userService.getAllUser();
        return ResponseEntity.ok(new ApiResponse("Success",allUser));
    }

    @PostMapping("/create-admin-user")
    public ResponseEntity<ApiResponse> createAdminUser(@RequestBody CreateUserRequest request){
        try {
            User adminUser = userService.createAdminUser(request);
            CreateUserDto userDto = modelMapper.map(adminUser, CreateUserDto.class);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse("User Created",userDto));
        } catch (Exception e){
            return ResponseEntity.status(CONFLICT).body(new ApiResponse(e.getMessage(), null));
        }
    }
}
