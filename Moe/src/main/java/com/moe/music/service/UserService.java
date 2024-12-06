package com.moe.music.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
import com.moe.music.dtoauth.UserRegisterResponseDTO;
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
	public UserRegisterResponseDTO register(RegisterRequestDTO request) {

		if (userJpa.findByEmail(request.getEmail().trim().toLowerCase()).isPresent()) {
			throw new AppException("Email already exists", HttpStatus.CONFLICT.value());
		}

		if (request.getPassword().equals(request.getConfirmPassword())) {
			throw new AppException("Password and confirm password must match!", 400);
		}

		Role role = roleJPA.findById(2) // 2 ~ ROLE USER
				.orElseThrow(() -> new AppException("Role not found", HttpStatus.NOT_FOUND.value()));

		User user = new User();
		user.setEmail(request.getEmail().trim().toLowerCase());
		user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
		user.setDisplayName(request.getDisplayName().trim());
		user.setBio(request.getBio() != null ? request.getBio().trim() : "");
		user.setProfilePictureUrl(request.getProfilePictureUrl() != null ? request.getProfilePictureUrl().trim() : "");
		user.setRole(role);

		String genderString = request.getGender() != null ? request.getGender().trim() : "";
		try {
			user.setGender(Gender.valueOf(genderString.toUpperCase()));
		} catch (IllegalArgumentException e) {
			user.setGender(Gender.PREFER_NOT_TO_SAY);
		}

		try {

			User savedUser = userJpa.save(user);

			UserRegisterResponseDTO userInfo = new UserRegisterResponseDTO();
			userInfo.setUserId(savedUser.getUserId());
			userInfo.setEmail(savedUser.getEmail());
			userInfo.setDisplayName(savedUser.getDisplayName());
			userInfo.setBio(savedUser.getBio());
			userInfo.setGender(savedUser.getGender());
			userInfo.setRoles(role.getRoleName());

			return userInfo;

		} catch (DataIntegrityViolationException e) {
			throw new AppException("Database constraint error: " + e.getRootCause().getMessage(),
					HttpStatus.CONFLICT.value());
		} catch (Exception e) {
			throw new AppException("An unexpected error occurred during registration: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}

	public LoginResponseDTO login(LoginRequestDTO request) {
		if (request.getEmail() == null || request.getEmail().isEmpty() || request.getPassword() == null
				|| request.getPassword().isEmpty()) {
			throw new AppException("Email and password must not be empty", HttpStatus.BAD_REQUEST.value());
		}

		Optional<User> user = userJpa.findByEmail(request.getEmail());

		if (user == null || !passwordEncoder.matches(request.getPassword(), user.get().getPasswordHash())) {
			throw new AppException("Invalid email or password", HttpStatus.UNAUTHORIZED.value());
		}

		try {
			String refreshToken = tokenService.generateRefreshToken(user.get());
			String accessToken = tokenService.generateJwtToken(user.get());

			LoginResponseDTO responseDTO = new LoginResponseDTO();
			responseDTO.setAccessToken(accessToken);
			responseDTO.setRefreshToken(refreshToken);

			Date expirationDate = tokenService.getExpirationDateFromJwtToken(accessToken);
			long expiresInSeconds = (expirationDate.getTime() - System.currentTimeMillis()) / 1000;
			long expiresInHours = expiresInSeconds / 3600;
			responseDTO.setAccessTokenExpiresIn(expiresInHours + " Giờ");

			LocalDateTime expirationDateRe = tokenService.getExpirationDateFromJwtRefreshToken(refreshToken);
			long expiresInSecondsRe = ChronoUnit.SECONDS.between(LocalDateTime.now(), expirationDateRe);
			long expiresInHoursRe = expiresInSecondsRe / 3600;
			responseDTO.setRefreshTokenExpiresIn(expiresInHoursRe + " Giờ");

			UserRegisterResponseDTO userInfo = new UserRegisterResponseDTO();
			userInfo.setUserId(user.get().getUserId());
			userInfo.setEmail(user.get().getEmail());
			userInfo.setDisplayName(user.get().getDisplayName());
			userInfo.setRoles(user.get().getRole().getRoleName());
			userInfo.setBio(user.get().getBio());
			userInfo.setGender(user.get().getGender());
			userInfo.setProfilePictureUrl(user.get().getProfilePictureUrl());

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

	public User findByEmail(String email) {
		return userJpa.findByEmail(email)
				.orElseThrow(() -> new EntityNotFoundException("User with email " + email + " not found"));
	}

	public User findByResetToken(String token) {
		if (token == null || token.isEmpty()) {
			throw new IllegalArgumentException("Reset token cannot be null or empty");
		}

		return userJpa.findByPasswordResetToken(token)
				.orElseThrow(() -> new EntityNotFoundException("User with reset token not found or token is expired"));
	}

	public void updatePassword(User user, String newPassword) {
		if (user == null) {
			throw new IllegalArgumentException("User cannot be null");
		}

		if (newPassword == null || newPassword.isEmpty()) {
			throw new IllegalArgumentException("New password cannot be null or empty");
		}

		user.setPasswordHash(passwordEncoder.encode(newPassword));
		user.setPasswordResetToken(null);
		user.setPasswordResetExpires(null);
		userJpa.save(user);
	}

	public void save(User user) {
		if (user == null) {
			throw new IllegalArgumentException("User cannot be null");
		}
		userJpa.save(user);
	}

	public void logOut(User user) {
		if (user == null) {
			throw new IllegalArgumentException("User cannot be null");
		}
		tokenService.clearTokens(user);
	}
}
