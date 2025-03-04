package com.moe.music.dto;

import java.util.Set;

import com.moe.music.model.User.Gender;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterResponseDTO {
	private Long userId;
	private String email;
	private String userName;
	private String profilePictureUrl;
	private String bio;
	private Gender gender;
	private String provider;
	private Set<String> roles;
}
