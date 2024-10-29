package com.moe.music.dtoauth;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}