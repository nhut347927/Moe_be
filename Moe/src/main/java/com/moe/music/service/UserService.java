package com.moe.music.service;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
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
import com.moe.music.model.User.Gender;

import jakarta.persistence.EntityNotFoundException;
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

		if (userJpa.findByEmail(request.getEmail()) != null) {
			throw new AppException("Email already exists", HttpStatus.CONFLICT.value());
		}

		Optional<Role> role;
		try {
			role = roleJPA.findById(4); // 4 ~ ROLE_GUEST
		} catch (EntityNotFoundException e) {
			throw new AppException("Role not found", HttpStatus.NOT_FOUND.value());
		}

		User user = new User();
		user.setEmail(request.getEmail());
		user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
		user.setDisplayName(request.getDisplayName());
		user.setBio(request.getBio());

		try {
			String genderString = request.getGender().toString();
			Gender gender;
			if (genderString != null) {

				gender = Gender.valueOf(genderString.toUpperCase());
			} else {

				gender = Gender.PREFER_NOT_TO_SAY;
			}
			user.setGender(gender);
		} catch (IllegalArgumentException e) {
			user.setGender(Gender.PREFER_NOT_TO_SAY);
		}

		user.setProfilePictureUrl(request.getProfilePictureUrl());
		user.setRole(role.get());

		try {
			return userJpa.save(user);
		} catch (DataIntegrityViolationException e) {
			throw new AppException("Database error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
		} catch (Exception e) {
			throw new AppException("An error occurred during registration: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
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
			userInfo.setRoles(user.getRole().getRoleName());

			responseDTO.setUser(userInfo);
			return responseDTO;
		} catch (Exception e) {
			throw new AppException("Failed to generate tokens: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}

	@Transactional
	public void changePassword(User user, String newPassword) {

		if (newPassword == null || newPassword.trim().isEmpty()) {
			throw new AppException("New password cannot be empty", HttpStatus.BAD_REQUEST.value());
		}

		user.setPasswordHash(passwordEncoder.encode(newPassword));

		try {
			userJpa.save(user);
		} catch (Exception e) {

			throw new AppException("Failed to change password: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}

	public boolean validateOldPassword(User user, String oldPassword) {

		User existingUser = userJpa.findById(user.getUserId())
				.orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND.value()));

		if (oldPassword == null || oldPassword.trim().isEmpty()) {
			throw new AppException("Old password cannot be empty", HttpStatus.BAD_REQUEST.value());
		}

		boolean isMatch = passwordEncoder.matches(oldPassword, existingUser.getPasswordHash());
		if (!isMatch) {
			throw new AppException("Old password is incorrect", HttpStatus.UNAUTHORIZED.value());
		}

		return true;
	}
}
