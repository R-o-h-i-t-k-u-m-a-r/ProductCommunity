package com.productcommunity.dto;

import com.productcommunity.model.Role;
import lombok.Data;

import java.util.Collection;

@Data
public class CreateUserDto {
    private String firstName;
    private String lastName;
    private String userName;
    private String password;
    Collection<Role> roles;
}

