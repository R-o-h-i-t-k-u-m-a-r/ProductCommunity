package com.productcommunity.testcontroller;

import com.productcommunity.controller.UserController;
import com.productcommunity.dto.UserDTO;
import com.productcommunity.dto.UserImageDto;
import com.productcommunity.enums.ERole;
import com.productcommunity.exceptions.ResourceNotFoundException;
import com.productcommunity.model.Role;
import com.productcommunity.model.User;
import com.productcommunity.request.UserUpdateRequest;
import com.productcommunity.response.ApiResponse;
import com.productcommunity.service.user.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Disabled("tested")
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private IUserService userService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private UserController userController;

    private UserDTO userDTO;
    private User user;
    private UserUpdateRequest updateRequest;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        UserImageDto userImageDto = new UserImageDto();
        userImageDto.setId(1L);
        userImageDto.setDownloadUrl("http://example.com/avatar.jpg");

        userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setFirstName("John");
        userDTO.setLastName("Doe");
        userDTO.setUserName("johndoe");
        Role userRole = new Role();
        userRole.setName(ERole.ROLE_ADMIN);
        userDTO.setRoles(Collections.singletonList(userRole));
        userDTO.setUserImage(userImageDto);

        user = new User();
        user.setId(1L);
        user.setUserName("johndoe");

        updateRequest = new UserUpdateRequest();
        updateRequest.setFirstName("John");
        updateRequest.setLastName("Doe");

        // ✅ Use real authentication token that returns true for isAuthenticated()
        authentication = new UsernamePasswordAuthenticationToken("johndoe", null, Collections.emptyList());
    }

    @Test
    void getCurrentUser_Success() throws ResourceNotFoundException {
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(userService.getByUserName("johndoe")).thenReturn(userDTO);

        ResponseEntity<ApiResponse> response = userController.getCurrentUser();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Success", response.getBody().getMessage());

        UserDTO responseUser = (UserDTO) response.getBody().getData();
        assertNotNull(responseUser);
        assertEquals(userDTO.getId(), responseUser.getId());
        assertEquals("John", responseUser.getFirstName());
        assertEquals("Doe", responseUser.getLastName());
        assertEquals("johndoe", responseUser.getUserName());
        assertEquals(1, responseUser.getRoles().size());
        assertEquals("http://example.com/avatar.jpg", responseUser.getUserImage().getDownloadUrl());
    }

    @Test
    void getCurrentUser_NotFound() throws ResourceNotFoundException {
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(userService.getByUserName("johndoe")).thenThrow(new ResourceNotFoundException("User not found"));

        ResponseEntity<ApiResponse> response = userController.getCurrentUser();

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found", response.getBody().getMessage());
    }

    @Test
    void updateUser_Success() throws ResourceNotFoundException {
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(userService.updateUser(any(UserUpdateRequest.class), anyString())).thenReturn(user);
        when(userService.convertUserToDto(user)).thenReturn(userDTO);

        ResponseEntity<ApiResponse> response = userController.updateUser(updateRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Update User Success!", response.getBody().getMessage());

        UserDTO updatedUser = (UserDTO) response.getBody().getData();
        assertNotNull(updatedUser);
        assertEquals("John", updatedUser.getFirstName());
        assertEquals("Doe", updatedUser.getLastName());
    }

    @Test
    void updateUser_NotFound() throws ResourceNotFoundException {
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(userService.updateUser(any(UserUpdateRequest.class), anyString()))
                .thenThrow(new ResourceNotFoundException("User not found"));

        ResponseEntity<ApiResponse> response = userController.updateUser(updateRequest);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found", response.getBody().getMessage());
    }

    @Test
    void deleteUser_Success() throws ResourceNotFoundException {
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        doNothing().when(userService).deleteUser("johndoe");

        ResponseEntity<ApiResponse> response = userController.deleteUser();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Delete User Success!", response.getBody().getMessage());
        assertNull(response.getBody().getData());
        verify(userService, times(1)).deleteUser("johndoe");
    }

    @Test
    void deleteUser_NotFound() throws ResourceNotFoundException {
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        doThrow(new ResourceNotFoundException("User not found")).when(userService).deleteUser("johndoe");

        ResponseEntity<ApiResponse> response = userController.deleteUser();

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found", response.getBody().getMessage());
    }

    @Test
    void getCurrentUser_Unauthenticated() {
        SecurityContextHolder.clearContext(); // no auth

        ResponseEntity<ApiResponse> response = userController.getCurrentUser();

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("User not authenticated", response.getBody().getMessage());
    }

    @Test
    void updateUser_WithAllFields() throws ResourceNotFoundException {
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUserName("johndoe");
        updatedUser.setFirstName("John");
        updatedUser.setLastName("Doe");

        UserDTO updatedUserDTO = new UserDTO();
        updatedUserDTO.setId(1L);
        updatedUserDTO.setFirstName("John");
        updatedUserDTO.setLastName("Doe");
        updatedUserDTO.setUserName("johndoe");

        when(userService.updateUser(any(UserUpdateRequest.class), anyString())).thenReturn(updatedUser);
        when(userService.convertUserToDto(updatedUser)).thenReturn(updatedUserDTO);

        ResponseEntity<ApiResponse> response = userController.updateUser(updateRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        UserDTO responseUser = (UserDTO) response.getBody().getData();
        assertEquals("John", responseUser.getFirstName());
        assertEquals("Doe", responseUser.getLastName());
    }
}
