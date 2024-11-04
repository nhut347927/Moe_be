package com.moe.music.dtoauth;

import lombok.Data;

@Data
public class ChangePasswordRequestDTO {
    private String oldPassword;
    private String newPassword;
}