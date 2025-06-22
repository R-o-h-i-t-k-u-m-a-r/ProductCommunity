package com.productcommunity.response;

import com.productcommunity.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class JwtResponse {
    private String message;
    private String jwtToken;
    private UserDTO userDto;
}
