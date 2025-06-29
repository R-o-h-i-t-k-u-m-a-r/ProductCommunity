package com.productcommunity.controller;

import com.productcommunity.dto.CreateUserDto;
import com.productcommunity.dto.UserDTO;
import com.productcommunity.exceptions.AlreadyExistsException;
import com.productcommunity.exceptions.ResourceNotFoundException;
import com.productcommunity.model.User;
import com.productcommunity.repository.UserRepository;
import com.productcommunity.request.CreateUserRequest;
import com.productcommunity.request.LoginRequest;
import com.productcommunity.request.PasswordResetRequest;
import com.productcommunity.response.ApiResponse;
import com.productcommunity.response.JwtResponse;
import com.productcommunity.security.jwt.JwtUtil;
import com.productcommunity.security.user.UserDetailsServiceImpl;
import com.productcommunity.service.email.EmailService;
import com.productcommunity.service.email.EmailVerificationService;
import com.productcommunity.service.email.PasswordResetService;
import com.productcommunity.service.user.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("${api.prefix}/public")
public class PublicController {

    private final IUserService userService;
    private final ModelMapper modelMapper;
    private final UserDetailsServiceImpl userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    private final EmailService emailService;
    private final EmailVerificationService emailVerificationService;
    private final UserRepository userRepository;
    private final PasswordResetService passwordResetService;

    @GetMapping("/health-check")
    public ResponseEntity<ApiResponse> healthCheck() {
        return ResponseEntity.ok(new ApiResponse("Application is running", null));
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> createUser(@RequestBody @Valid CreateUserRequest request) {
        try {
            User user = userService.createUser(request);
            //User user = userService.createAdminUser(request);

            emailVerificationService.sendVerificationEmail(user);

            CreateUserDto createUserDto = modelMapper.map(user, CreateUserDto.class);

            emailService.sendEmail(user.getUserName(), "ProductCommunity User Account Registration", "User Account is created Successfully");


            return ResponseEntity.ok(new ApiResponse("User created successfully. Please check your email for verification.", createUserDto));
        } catch (AlreadyExistsException | ResourceNotFoundException e) {
            return ResponseEntity.status(CONFLICT).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUserName(), request.getPassword()));
            //SecurityContextHolder.getContext().setAuthentication(authentication);
            //UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUserName());

            String jwt = jwtUtil.generateToken(userDetails.getUsername());
            UserDTO userDto = userService.getByUserName(userDetails.getUsername());

            return new ResponseEntity<>(new JwtResponse("user " + userDetails.getUsername() + " logged in successfully!!", jwt, userDto), HttpStatus.OK);

        } catch (Exception e) {
            log.error("Exception occurred while createAuthenticationToken ", e);
            return ResponseEntity.status(BAD_REQUEST).body(new ApiResponse("Incorrect username or password", null));
        }
    }

    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse> verifyEmail(@RequestParam String token) {
        try {
            emailVerificationService.verifyUser(token);
            return ResponseEntity.ok(new ApiResponse(
                    "Email verified successfully!",
                    null
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(BAD_REQUEST).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse> resendVerification(@RequestParam String email) {
        if(!userRepository.existsByUserName(email)){
            throw new ResourceNotFoundException("User not found");
        }
        User user = userRepository.findByUserName(email);
        if (user.isEnabled()) {
            return ResponseEntity.badRequest().body(new ApiResponse("Email already verified", null));
        }

        emailVerificationService.sendVerificationEmail(user);
        return ResponseEntity.ok(new ApiResponse("Verification email resent", null));
    }


    @Operation(summary = "Initiate password reset")
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(@RequestParam String email) {
        try {
            passwordResetService.initiatePasswordReset(email);
            return ResponseEntity.ok(new ApiResponse(
                    "Password reset link sent to your email",
                    null
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @Operation(summary = "Reset password")
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(
            @RequestParam String token,
            @RequestBody @Valid PasswordResetRequest request) {
        try {
            passwordResetService.resetPassword(token, request.getNewPassword());
            return ResponseEntity.ok(new ApiResponse(
                    "Password reset successfully",
                    null
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    
}
