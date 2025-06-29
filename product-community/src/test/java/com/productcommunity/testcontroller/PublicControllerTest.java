package com.productcommunity.testcontroller;

import com.productcommunity.controller.PublicController;
import com.productcommunity.dto.CreateUserDto;
import com.productcommunity.dto.UserDTO;
import com.productcommunity.exceptions.AlreadyExistsException;
import com.productcommunity.exceptions.ResourceNotFoundException;
import com.productcommunity.model.Role;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.*;

@Disabled("tested")
@ExtendWith(MockitoExtension.class)
public class PublicControllerTest {

    @Mock private IUserService userService;
    @Mock private ModelMapper modelMapper;
    @Mock private UserDetailsServiceImpl userDetailsService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtUtil jwtUtil;
    @Mock private EmailService emailService;
    @Mock private EmailVerificationService emailVerificationService;
    @Mock private UserRepository userRepository;
    @Mock private PasswordResetService passwordResetService;

    @InjectMocks
    private PublicController publicController;

    private CreateUserRequest createUserRequest;
    private User user;

    @BeforeEach
    void setup() {
        createUserRequest = new CreateUserRequest();
        createUserRequest.setFirstName("John");
        createUserRequest.setLastName("Doe");
        createUserRequest.setUserName("johndoe@example.com");
        createUserRequest.setPassword("password123");

        user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUserName("johndoe@example.com");
        user.setRoles(Collections.singletonList(new Role()));
        user.setEnabled(false);
    }

    @Test
    void healthCheck_returnsRunningStatus() {
        ResponseEntity<ApiResponse> response = publicController.healthCheck();
        assertEquals(OK, response.getStatusCode());
        assertEquals("Application is running", response.getBody().getMessage());
    }

    @Test
    void createUser_success() throws Exception {
        CreateUserDto dto = new CreateUserDto();
        dto.setUserName("johndoe@example.com");

        when(userService.createUser(createUserRequest)).thenReturn(user);
        when(modelMapper.map(user, CreateUserDto.class)).thenReturn(dto);

        ResponseEntity<ApiResponse> response = publicController.createUser(createUserRequest);
        assertEquals(OK, response.getStatusCode());
        assertEquals("User created successfully. Please check your email for verification.", response.getBody().getMessage());
        assertEquals(dto.getUserName(), ((CreateUserDto) response.getBody().getData()).getUserName());

        verify(emailVerificationService).sendVerificationEmail(user);
        verify(emailService).sendEmail(eq("johndoe@example.com"), anyString(), anyString());
    }

    @Test
    void createUser_conflict() throws Exception {
        when(userService.createUser(createUserRequest)).thenThrow(new AlreadyExistsException("User already exists"));

        ResponseEntity<ApiResponse> response = publicController.createUser(createUserRequest);
        assertEquals(CONFLICT, response.getStatusCode());
        assertEquals("User already exists", response.getBody().getMessage());
    }

    @Test
    void login_success() {
        LoginRequest request = new LoginRequest();
        request.setUserName("johndoe");
        request.setPassword("password");

        Authentication auth = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);
        UserDTO userDTO = new UserDTO();

        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(userDetailsService.loadUserByUsername("johndoe")).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("johndoe");
        when(jwtUtil.generateToken("johndoe")).thenReturn("mockJwtToken");
        when(userService.getByUserName("johndoe")).thenReturn(userDTO);

        ResponseEntity<?> response = publicController.login(request);

        assertEquals(OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof JwtResponse);
        assertEquals("mockJwtToken", ((JwtResponse) response.getBody()).getJwtToken());
    }

    @Test
    void login_failure() {
        LoginRequest request = new LoginRequest();
        request.setUserName("wrong");
        request.setPassword("wrong");

        when(authenticationManager.authenticate(any())).thenThrow(new RuntimeException("Invalid login"));

        ResponseEntity<?> response = publicController.login(request);

        assertEquals(BAD_REQUEST, response.getStatusCode());
        assertEquals("Incorrect username or password", ((ApiResponse) response.getBody()).getMessage());
    }

    @Test
    void verifyEmail_success() {
        ResponseEntity<ApiResponse> response = publicController.verifyEmail("valid-token");
        assertEquals(OK, response.getStatusCode());
        assertEquals("Email verified successfully!", response.getBody().getMessage());
    }

    @Test
    void verifyEmail_failure() {
        doThrow(new RuntimeException("Invalid token")).when(emailVerificationService).verifyUser("invalid");

        ResponseEntity<ApiResponse> response = publicController.verifyEmail("invalid");
        assertEquals(BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid token", response.getBody().getMessage());
    }

    @Test
    void resendVerification_success() {
        when(userRepository.existsByUserName("john@example.com")).thenReturn(true);
        when(userRepository.findByUserName("john@example.com")).thenReturn(user);

        ResponseEntity<ApiResponse> response = publicController.resendVerification("john@example.com");
        assertEquals(OK, response.getStatusCode());
        assertEquals("Verification email resent", response.getBody().getMessage());

        verify(emailVerificationService).sendVerificationEmail(user);
    }

    @Test
    void resendVerification_alreadyVerified() {
        user.setEnabled(true);
        when(userRepository.existsByUserName("john@example.com")).thenReturn(true);
        when(userRepository.findByUserName("john@example.com")).thenReturn(user);

        ResponseEntity<ApiResponse> response = publicController.resendVerification("john@example.com");
        assertEquals(BAD_REQUEST, response.getStatusCode());
        assertEquals("Email already verified", response.getBody().getMessage());
    }

    @Test
    void forgotPassword_success() {
        ResponseEntity<ApiResponse> response = publicController.forgotPassword("john@example.com");
        assertEquals(OK, response.getStatusCode());
        assertEquals("Password reset link sent to your email", response.getBody().getMessage());
    }

    @Test
    void forgotPassword_failure() {
        doThrow(new RuntimeException("Email not found")).when(passwordResetService).initiatePasswordReset("missing@example.com");

        ResponseEntity<ApiResponse> response = publicController.forgotPassword("missing@example.com");
        assertEquals(BAD_REQUEST, response.getStatusCode());
        assertEquals("Email not found", response.getBody().getMessage());
    }

    @Test
    void resetPassword_success() {
        PasswordResetRequest request = new PasswordResetRequest();
        request.setNewPassword("newpass123");

        ResponseEntity<ApiResponse> response = publicController.resetPassword("valid-token", request);
        assertEquals(OK, response.getStatusCode());
        assertEquals("Password reset successfully", response.getBody().getMessage());
    }

    @Test
    void resetPassword_failure() {
        PasswordResetRequest request = new PasswordResetRequest();
        request.setNewPassword("fail");

        doThrow(new RuntimeException("Invalid token")).when(passwordResetService).resetPassword("invalid-token", "fail");

        ResponseEntity<ApiResponse> response = publicController.resetPassword("invalid-token", request);
        assertEquals(BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid token", response.getBody().getMessage());
    }
}

