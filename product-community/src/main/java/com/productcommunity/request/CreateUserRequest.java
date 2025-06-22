package com.productcommunity.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class CreateUserRequest {
    @NotEmpty
    private String firstName;
    @NotEmpty
    private String lastName;
    @NotEmpty
    private String userName;
    @NotEmpty
    private String password;
}

