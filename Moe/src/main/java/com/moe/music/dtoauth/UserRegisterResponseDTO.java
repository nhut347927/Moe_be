package com.moe.music.dtoauth;

import com.moe.music.model.User.Gender;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterResponseDTO {
	private Integer userId;
	private String email;
	private String displayName;
	private String profilePictureUrl;
	private String bio;
	private Gender gender;
	private String roles;
}
