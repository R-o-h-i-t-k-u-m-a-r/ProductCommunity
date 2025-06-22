package com.productcommunity.service.user;

import com.productcommunity.dto.UserDTO;
import com.productcommunity.dto.UserImageDto;
import com.productcommunity.enums.ERole;
import com.productcommunity.exceptions.AlreadyExistsException;
import com.productcommunity.exceptions.ResourceNotFoundException;
import com.productcommunity.model.Role;
import com.productcommunity.model.User;
import com.productcommunity.model.UserImage;
import com.productcommunity.repository.RoleRepository;
import com.productcommunity.repository.UserImageRepository;
import com.productcommunity.repository.UserRepository;
import com.productcommunity.request.CreateUserRequest;
import com.productcommunity.request.UserUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService{

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final RoleRepository roleRepository;
    private final UserImageRepository imageRepository;

    public static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));
    }

    @Transactional
    @Override
    public User createUser(CreateUserRequest request) {
        return  Optional.of(request)
                .filter(user -> !userRepository.existsByUserName(request.getUserName()))
                .map(req -> {
                    User user = new User();
                    user.setUserName(request.getUserName());
                    user.setPassword(passwordEncoder.encode(request.getPassword()));
                    user.setFirstName(request.getFirstName());
                    user.setLastName(request.getLastName());

                    // Handle roles
                    Collection<Role> roles = new HashSet<>();
                    Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                            .orElseGet(() -> {
                                Role newRole = new Role();
                                newRole.setName(ERole.ROLE_USER);
                                return roleRepository.save(newRole);
                            });
                    roles.add(userRole);

                    user.setRoles(roles);

                    return  userRepository.save(user);
                }) .orElseThrow(() -> new AlreadyExistsException("Oops!" +request.getUserName() +" already exists!"));
    }

    @Transactional
    @Override
    public User createAdminUser(CreateUserRequest request){
        return Optional.of(request)
                .filter(user->!userRepository.existsByUserName(request.getUserName()))
                .map(req->{
                    User user = new User();
                    user.setUserName(request.getUserName());
                    user.setPassword(passwordEncoder.encode(request.getPassword()));
                    user.setFirstName(request.getFirstName());
                    user.setLastName(request.getLastName());


                    // Handle roles
                    Collection<Role> roles = new HashSet<>();
                    Role userRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                            .orElseGet(() -> {
                                Role newRole = new Role();
                                newRole.setName(ERole.ROLE_ADMIN);
                                return roleRepository.save(newRole);
                            });
                    roles.add(userRole);

                    user.setRoles(roles);
                    return userRepository.save(user);
                }).orElseThrow(()-> new AlreadyExistsException("Oops!" +request.getUserName() +" already exists!"));
    }

    @Override
    public User updateUser(UserUpdateRequest request, String userName) {
        return Optional.of(userRepository.findByUserName(userName))
                .map(existingUser->{
                    existingUser.setFirstName(request.getFirstName());
                    existingUser.setLastName(request.getLastName());

                    return userRepository.save(existingUser);
                }).orElseThrow(()->new ResourceNotFoundException("User not Found!"));

    }

    @Transactional
    @Override
    public void deleteUser(String userName)  {

        userRepository.deleteByUserName(userName);
    }

    @Override
    public UserDTO convertUserToDto(User user) {
        UserImage image = imageRepository.findByUserId(user.getId());
        UserImageDto imageDto = modelMapper.map(image, UserImageDto.class);

        UserDTO userDto = modelMapper.map(user, UserDTO.class);
        userDto.setUserImage(imageDto);

        return userDto;
    }

    @Override
    public List<UserDTO> getAllUser(){
        List<User> users = userRepository.findAll();
        return users.stream().map(user -> modelMapper.map(user, UserDTO.class)).toList();
    }

    @Override
    public UserDTO getByUserName(String userName) {
        User user = Optional.of(userRepository.findByUserName(userName))
                .orElseThrow(() -> new ResourceNotFoundException("User not found!!!"));
        return modelMapper.map(user,UserDTO.class);
    }

    @Override
    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        return userRepository.findByUserName(userName);

    }
}
