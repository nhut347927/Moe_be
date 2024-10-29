package com.moe.music.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.moe.music.dtoauth.LoginRequest;
import com.moe.music.dtoauth.RegisterRequest;
import com.moe.music.jpa.UserJPA;
import com.moe.music.model.User;
import com.moe.music.exception.AppException;
import org.springframework.http.HttpStatus;

import jakarta.transaction.Transactional;

@Service
public class UserService {

	@Autowired
	private UserJPA userJpa;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private TokenService tokenService;

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
			return tokenService.generateJwtToken(user); // Sử dụng TokenService để tạo token
		}
		throw new AppException("Invalid username or password", HttpStatus.UNAUTHORIZED.value());
	}

	@Transactional
	public void changePassword(User user, String newPassword) {
		user.setPasswordHash(passwordEncoder.encode(newPassword));
		userJpa.save(user);
	}

	public boolean validateOldPassword(User user, String oldPassword) {
		User existingUser = userJpa.findById(user.getUserId())
				.orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND.value()));

		// Kiểm tra mật khẩu cũ
		return passwordEncoder.matches(oldPassword, existingUser.getPasswordHash());
	}
}
