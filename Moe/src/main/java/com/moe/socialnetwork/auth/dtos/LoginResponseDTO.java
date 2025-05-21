package com.moe.socialnetwork.auth.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    private String accessToken;
    private String accessTokenExpiresIn;
    private String refreshToken;
    private String refreshTokenExpiresIn;
    private String provider;
    private UserRegisterResponseDTO user;
}
