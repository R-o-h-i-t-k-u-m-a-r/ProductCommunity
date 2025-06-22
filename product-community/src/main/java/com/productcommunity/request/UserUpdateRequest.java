package com.productcommunity.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class UserUpdateRequest {
    @NotEmpty(message = "Please fill first name field")
    private String firstName;
    @NotEmpty(message = "Please fill lastName field")
    private String lastName;
}
