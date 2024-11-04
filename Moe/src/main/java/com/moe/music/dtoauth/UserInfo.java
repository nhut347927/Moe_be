package com.moe.music.dtoauth;

import java.time.LocalDateTime;
import java.util.List;

import com.moe.music.model.User.Gender;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {
	private Integer userId;
	private String email;
	private String displayName;
	private String profilePictureUrl;
	private String bio;
	private Gender gender;
	private List<String> roles;
}
