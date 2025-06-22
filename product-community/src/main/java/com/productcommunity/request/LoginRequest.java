package com.productcommunity.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class LoginRequest {
    @NotEmpty
    private String userName;
    @NotEmpty
    private String password;
}
