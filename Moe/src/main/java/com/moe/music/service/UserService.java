package com.moe.music.service;

import java.util.Date;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.moe.music.dtoauth.LoginRequestDTO;
import com.moe.music.dtoauth.LoginResponseDTO;
import com.moe.music.dtoauth.RegisterRequestDTO;
import com.moe.music.dtoauth.UserInfo;
import com.moe.music.exception.AppException;
import com.moe.music.jpa.RoleJPA;
import com.moe.music.jpa.UserJPA;
import com.moe.music.model.Role;
import com.moe.music.model.User;

import jakarta.transaction.Transactional;

@Service
public class UserService {

	@Autowired
	private UserJPA userJpa;

	@Autowired
	private RoleJPA roleJPA;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private TokenService tokenService;

	@Transactional
	public User register(RegisterRequestDTO request) {
		Role role = roleJPA.getById(4); // 4~ ROLE GUEST
		User user = new User();
		user.setEmail(request.getEmail());
		user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
		user.setDisplayName(request.getDisplayName());
		user.setBio(request.getBio());
		user.setProfilePictureUrl(request.getProfilePictureUrl());
		user.setRole(role);
		return userJpa.save(user);
	}

	public LoginResponseDTO login(LoginRequestDTO request) {
		if (request.getEmail() == null || request.getEmail().isEmpty() || request.getPassword() == null
				|| request.getPassword().isEmpty()) {
			throw new AppException("Email and password must not be empty", HttpStatus.BAD_REQUEST.value());
		}

		User user = userJpa.findByEmail(request.getEmail());

		if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
			throw new AppException("Invalid email or password", HttpStatus.UNAUTHORIZED.value());
		}

		try {
			String refreshToken = tokenService.generateRefreshToken(user);
			String accessToken = tokenService.generateJwtToken(user);

			LoginResponseDTO responseDTO = new LoginResponseDTO();
			responseDTO.setAccessToken(accessToken);

			Date expirationDate = tokenService.getExpirationDateFromJwtToken(accessToken);
			long expiresInSeconds = (expirationDate.getTime() - System.currentTimeMillis()) / 1000;
			long expiresInHours = expiresInSeconds / 3600; // Chia cho 3600 để chuyển đổi từ giây sang giờ
			responseDTO.setExpiresIn(expiresInHours + " Giờ");

			UserInfo userInfo = new UserInfo();
			userInfo.setUserId(user.getUserId());
			userInfo.setEmail(user.getEmail());
			userInfo.setDisplayName(user.getDisplayName());
			userInfo.setRoles(user.getRole().getRolePermission().stream()
					.map(rolePermission -> rolePermission.getPermission().getActionName())
					.collect(Collectors.toList()));

			responseDTO.setUser(userInfo);
			return responseDTO;
		} catch (Exception e) {
			throw new AppException("Failed to generate tokens: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
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
