package com.moe.music.dtoauth;

import lombok.Data;

@Data
public class RegisterRequest {
	private String email;
	private String password;
	private String displayName;
	private String bio;
	private String profilePictureUrl;
	// Thêm các trường khác nếu cần
}