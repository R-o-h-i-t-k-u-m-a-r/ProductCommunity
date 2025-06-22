package com.productcommunity.service.user;

import com.productcommunity.dto.UserDTO;
import com.productcommunity.model.User;
import com.productcommunity.request.CreateUserRequest;
import com.productcommunity.request.UserUpdateRequest;

import java.util.List;

public interface IUserService {
    User getUserById(Long userId);
    User createUser(CreateUserRequest request);

    User createAdminUser(CreateUserRequest request);

    User updateUser(UserUpdateRequest request, String userName);

    void deleteUser(String userName);

    UserDTO convertUserToDto(User user);

    List<UserDTO> getAllUser();

    UserDTO getByUserName(String userName);

    User getAuthenticatedUser();
}
