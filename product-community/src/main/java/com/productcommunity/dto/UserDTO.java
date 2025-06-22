package com.productcommunity.dto;

import com.productcommunity.model.Role;
import lombok.Data;

import java.util.Collection;

@Data
public class UserDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String userName;
    Collection<Role> roles;
    private UserImageDto userImage;
}