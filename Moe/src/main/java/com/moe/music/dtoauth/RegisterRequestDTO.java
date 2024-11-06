package com.moe.music.dtoauth;

import com.moe.music.model.User.Gender;

import lombok.Data;

@Data
public class RegisterRequestDTO {
	private String email;
	private String password;
	private String displayName;
	private String bio;
	private Gender gender;
	private String profilePictureUrl;
}