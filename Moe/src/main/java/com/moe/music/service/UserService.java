package com.moe.music.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.moe.music.dtoauth.LoginRequest;
import com.moe.music.dtoauth.RegisterRequest;
import com.moe.music.jpa.UserJPA;
import com.moe.music.model.User;
import com.moe.music.security.JwtTokenProvider;

import jakarta.transaction.Transactional;

@Service
public class UserService {

	@Autowired
	private UserJPA userJpa;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	@Transactional
	public User register(RegisterRequest request) {
		User user = new User();
		user.setUsername(request.getUsername());
		user.setEmail(request.getEmail());
		user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
		user.setDisplayName(request.getDisplayName());
		user.setBio(request.getBio());
		user.setProfilePictureUrl(request.getProfilePictureUrl());
		return userJpa.save(user);
	}

	public String login(LoginRequest request) {
		User user = userJpa.findByUsername(request.getUsername());
		if (user != null && passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
			return jwtTokenProvider.generateToken(user);
		}
		throw new RuntimeException("Invalid username or password");
	}

	@Transactional
	public void changePassword(User user, String newPassword) {
		user.setPasswordHash(passwordEncoder.encode(newPassword));
		userJpa.save(user);
	}

	public boolean validateOldPassword(User userId, String oldPassword) {
		User user = userJpa.findById(userId.getUserId()).orElseThrow(() -> new RuntimeException("User not found"));

		// Kiểm tra mật khẩu cũ
		return passwordEncoder.matches(oldPassword, user.getPasswordHash());
	}
}
