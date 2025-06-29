package com.productcommunity.service;

import com.productcommunity.dto.UserDTO;
import com.productcommunity.dto.UserImageDto;
import com.productcommunity.enums.ERole;
import com.productcommunity.exceptions.AlreadyExistsException;
import com.productcommunity.exceptions.ResourceNotFoundException;
import com.productcommunity.model.*;
import com.productcommunity.repository.RoleRepository;
import com.productcommunity.repository.UserImageRepository;
import com.productcommunity.repository.UserRepository;
import com.productcommunity.request.CreateUserRequest;
import com.productcommunity.request.UserUpdateRequest;
import com.productcommunity.service.user.UserService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserImageRepository imageRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Disabled("tested")
    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() {
        // Arrange
        Long userId = 1L;
        User mockUser = new User();
        mockUser.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        // Act
        User result = userService.getUserById(userId);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getId());
    }

    @Disabled("tested")
    @Test
    void getUserById_WhenUserNotExists_ShouldThrowException() {
        // Arrange
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(userId));
    }

    @Disabled("tested")
    @Test
    void createUser_WhenUsernameNotAvailable_ShouldCreateUser() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUserName("newuser");
        request.setPassword("password");
        request.setFirstName("John");
        request.setLastName("Doe");

        Role userRole = new Role();
        userRole.setName(ERole.ROLE_USER);

        when(userRepository.existsByUserName(request.getUserName())).thenReturn(false);
        //when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = userService.createUser(request);

        // Assert
        assertNotNull(result);
        assertEquals("newuser", result.getUserName());
        assertNotEquals("encodedPassword", result.getPassword());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertTrue(result.getRoles().stream().anyMatch(r -> r.getName() == ERole.ROLE_USER));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Disabled("tested")
    @Test
    void createUser_WhenUsernameExists_ShouldThrowException() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUserName("existinguser");

        when(userRepository.existsByUserName(request.getUserName())).thenReturn(true);

        // Act & Assert
        assertThrows(AlreadyExistsException.class, () -> userService.createUser(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Disabled("tested")
    @Test
    void createUser_WhenRoleNotExists_ShouldCreateRole() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUserName("newuser");
        request.setPassword("password");

        Role newRole = new Role();
        newRole.setName(ERole.ROLE_USER);

        when(userRepository.existsByUserName(request.getUserName())).thenReturn(false);
        //when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenReturn(newRole);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = userService.createUser(request);

        // Assert
        assertNotNull(result);
        verify(roleRepository, times(1)).save(any(Role.class));
    }

    @Disabled("tested")
    @Test
    void createAdminUser_ShouldCreateUserWithAdminRole() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUserName("admin");
        request.setPassword("adminpass");

        Role adminRole = new Role();
        adminRole.setName(ERole.ROLE_ADMIN);

        when(userRepository.existsByUserName(request.getUserName())).thenReturn(false);
        //when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(roleRepository.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = userService.createAdminUser(request);

        // Assert
        assertNotNull(result);
        assertTrue(result.getRoles().stream().anyMatch(r -> r.getName() == ERole.ROLE_ADMIN));
    }

    @Disabled("tested")
    @Test
    void updateUser_WhenUserExists_ShouldUpdateUser() {
        // Arrange
        String username = "existinguser";
        User existingUser = new User();
        existingUser.setUserName(username);
        existingUser.setFirstName("Old");
        existingUser.setLastName("Name");

        UserUpdateRequest request = new UserUpdateRequest();
        request.setFirstName("New");
        request.setLastName("Name");

        when(userRepository.findByUserName(username)).thenReturn(existingUser);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = userService.updateUser(request, username);

        // Assert
        assertNotNull(result);
        assertEquals("New", result.getFirstName());
        assertEquals("Name", result.getLastName());
        verify(userRepository, times(1)).save(existingUser);
    }

    @Disabled("tested")
    @Test
    void updateUser_WhenUserNotExists_ShouldThrowException() {
        // Arrange
        String username = "nonexistinguser";
        UserUpdateRequest request = new UserUpdateRequest();

        when(userRepository.findByUserName(username)).thenThrow(new ResourceNotFoundException(""));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(request, username));
        verify(userRepository, never()).save(any(User.class));
    }

    @Disabled("tested")
    @Test
    void deleteUser_ShouldCallRepositoryDelete() {
        // Arrange
        String username = "todelete";
        doNothing().when(userRepository).deleteByUserName(username);

        // Act
        userService.deleteUser(username);

        // Assert
        verify(userRepository, times(1)).deleteByUserName(username);
    }

    @Disabled("tested")
    @Test
    void convertUserToDto_ShouldMapCorrectly() {
        // Arrange
        User user = new User();
        user.setId(1L);
        UserImage image = new UserImage();
        UserImageDto imageDto = new UserImageDto();
        UserDTO expectedDto = new UserDTO();

        when(imageRepository.findByUserId(user.getId())).thenReturn(image);
        when(modelMapper.map(image, UserImageDto.class)).thenReturn(imageDto);
        when(modelMapper.map(user, UserDTO.class)).thenReturn(expectedDto);

        // Act
        UserDTO result = userService.convertUserToDto(user);

        // Assert
        assertSame(expectedDto, result);
        verify(imageRepository, times(1)).findByUserId(user.getId());
    }

    @Disabled("tested")
    @Test
    void getAllUser_ShouldReturnListOfUserDTOs() {
        // Arrange
        List<User> users = Arrays.asList(new User(), new User());
        when(userRepository.findAll()).thenReturn(users);
        when(modelMapper.map(any(User.class), eq(UserDTO.class))).thenReturn(new UserDTO());

        // Act
        List<UserDTO> result = userService.getAllUser();

        // Assert
        assertEquals(2, result.size());
        verify(userRepository, times(1)).findAll();
    }

    @Disabled("tested")
    @Test
    void getByUserName_WhenUserExists_ShouldReturnUserDTO() {
        // Arrange
        String username = "testuser";
        User user = new User();
        UserDTO expectedDto = new UserDTO();

        when(userRepository.findByUserName(username)).thenReturn(user);
        when(modelMapper.map(user, UserDTO.class)).thenReturn(expectedDto);

        // Act
        UserDTO result = userService.getByUserName(username);

        // Assert
        assertSame(expectedDto, result);
    }

    @Disabled("tested")
    @Test
    void getByUserName_WhenUserNotExists_ShouldThrowException() {
        // Arrange
        String username = "nonexistinguser";
        when(userRepository.findByUserName(username)).thenThrow(new ResourceNotFoundException(""));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.getByUserName(username));
    }

    @Disabled("tested")
    @Test
    void getAuthenticatedUser_ShouldReturnCurrentUser() {
        // Arrange
        String username = "currentuser";
        User expectedUser = new User();
        expectedUser.setUserName(username);

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(username);
        when(userRepository.findByUserName(username)).thenReturn(expectedUser);

        // Act
        User result = userService.getAuthenticatedUser();

        // Assert
        assertSame(expectedUser, result);
        assertEquals(username, result.getUserName());
    }
}
