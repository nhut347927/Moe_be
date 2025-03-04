package com.moe.music.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.moe.music.dto.LoginRequestDTO;
import com.moe.music.dto.LoginResponseDTO;
import com.moe.music.dto.RegisterRequestDTO;
import com.moe.music.dto.UserRegisterResponseDTO;
import com.moe.music.exception.AppException;
import com.moe.music.interfaces.UserInterface;
import com.moe.music.jpa.UserJPA;
import com.moe.music.model.User;
import com.moe.music.model.User.Gender;
import com.moe.music.utility.AuthorityUtil;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class UserService implements UserInterface {

	private UserJPA userJpa;

	private PasswordEncoder passwordEncoder;

	private TokenService tokenService;

	@Value("${google.client.id}")
	private String googleClientId;

	public UserService(UserJPA userJPA, PasswordEncoder passwordEncoder, TokenService tokenService) {
		this.userJpa = userJPA;
		this.passwordEncoder = passwordEncoder;
		this.tokenService = tokenService;
	};

	@Transactional
	public UserRegisterResponseDTO register(RegisterRequestDTO request) {

		if (userJpa.findByEmail(request.getEmail().trim().toLowerCase()).isPresent()) {
			throw new AppException("Email already exists", HttpStatus.CONFLICT.value());
		}

		if (userJpa.findByDisplayName(request.getEmail().trim().toLowerCase()).isPresent()) {
			throw new AppException("Display Name already exists", HttpStatus.CONFLICT.value());
		}

		if (!request.getPassword().equals(request.getConfirmPassword())) {
			throw new AppException("Password and confirm password must match!", 400);
		}
		String baseName = request.getEmail().split("@")[0];
		String uniqueUsername = baseName + System.currentTimeMillis();

		User user = new User();
		user.setEmail(request.getEmail().trim().toLowerCase());
		user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
		user.setName(request.getDisplayName().trim());
		user.setDisplayName(uniqueUsername);
		user.setProvider("NORMAL");
		user.setBio(request.getBio() != null ? request.getBio().trim() : "");
		user.setProfilePictureUrl(request.getProfilePictureUrl() != null ? request.getProfilePictureUrl().trim() : "");

		String genderString = request.getGender() != null ? request.getGender().trim() : "";
		try {
			user.setGender(Gender.valueOf(genderString.toUpperCase()));
		} catch (IllegalArgumentException e) {
			user.setGender(Gender.PREFER_NOT_TO_SAY);
		}

		try {

			User savedUser = userJpa.save(user);

			UserRegisterResponseDTO userInfo = new UserRegisterResponseDTO();
			userInfo.setUserId(savedUser.getId());
			userInfo.setEmail(savedUser.getEmail());
			userInfo.setUserName(savedUser.getUsername());
			userInfo.setBio(savedUser.getBio());
			userInfo.setGender(savedUser.getGender());
			userInfo.setProvider(savedUser.getProvider());
			userInfo.setRoles(AuthorityUtil.convertToAuthorities(savedUser.getRolePermissions()));

			return userInfo;

		} catch (DataIntegrityViolationException e) {
			throw new AppException("Database constraint error: " + e.getMessage(), HttpStatus.CONFLICT.value());
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

		if (!user.isPresent()) {
			throw new AppException("Email is not registered.", HttpStatus.NOT_FOUND.value());
		}

		if (user == null || !passwordEncoder.matches(request.getPassword(), user.get().getPasswordHash())) {
			throw new AppException("Invalid email or password", HttpStatus.UNAUTHORIZED.value());
		}

		try {
			String refreshToken = tokenService.generateRefreshToken(user.get());
			String accessToken = tokenService.generateJwtToken(user.get());

			LoginResponseDTO responseDTO = new LoginResponseDTO();
			responseDTO.setAccessToken(accessToken);
			responseDTO.setRefreshToken(refreshToken);

			LocalDateTime expirationDate = tokenService.getExpirationDateFromJwtToken(accessToken);
			long expiresInSeconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), expirationDate);
			long expiresInHours = expiresInSeconds / 3600;
			responseDTO.setAccessTokenExpiresIn(expiresInHours + " Giờ");

			LocalDateTime expirationDateRe = tokenService.getExpirationDateFromJwtRefreshToken(refreshToken);
			long expiresInSecondsRe = ChronoUnit.SECONDS.between(LocalDateTime.now(), expirationDateRe);
			long expiresInHoursRe = expiresInSecondsRe / 3600;
			responseDTO.setRefreshTokenExpiresIn(expiresInHoursRe + " Giờ");

			UserRegisterResponseDTO userInfo = new UserRegisterResponseDTO();
			userInfo.setUserId(user.get().getId());
			userInfo.setEmail(user.get().getEmail());
			userInfo.setUserName(user.get().getUsername());
			userInfo.setRoles(AuthorityUtil.convertToAuthorities(user.get().getRolePermissions()));
			userInfo.setBio(user.get().getBio());
			userInfo.setGender(user.get().getGender());
			userInfo.setProvider(user.get().getProvider());
			userInfo.setProfilePictureUrl(user.get().getProfilePictureUrl());

			responseDTO.setUser(userInfo);
			return responseDTO;
		} catch (Exception e) {
			throw new AppException("Failed to generate tokens: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}

	public LoginResponseDTO loginWithGoogle(String token) {

		HttpTransport transport = new NetHttpTransport();
		JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

		GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
				.setAudience(Collections.singletonList(googleClientId)).build();

		try {
			GoogleIdToken idToken = verifier.verify(token);
			if (idToken != null) {
				GoogleIdToken.Payload payload = idToken.getPayload();

				String email = payload.getEmail();
				String name = (String) payload.get("name");
				String pictureUrl = (String) payload.get("picture");

				Optional<User> userCheck = userJpa.findByEmail(email);
				User user;

				if (userCheck.isPresent()) {
					user = userCheck.get();
					if (!user.getProvider().equals("GOOGLE")) {
						throw new AppException("Email already exists", HttpStatus.CONFLICT.value());
					}
				} else {
					String baseName = email.split("@")[0];
					String uniqueUsername = baseName + System.currentTimeMillis();

					user = new User();
					user.setEmail(email);
					user.setPasswordHash(null);
					user.setName(name);
					user.setDisplayName(uniqueUsername);
					user.setProfilePictureUrl(pictureUrl);
					user.setProvider("GOOGLE");

					userJpa.save(user);
				}

				String refreshToken = tokenService.generateRefreshToken(user);
				String accessToken = tokenService.generateJwtToken(user);

				LoginResponseDTO responseDTO = new LoginResponseDTO();
				responseDTO.setAccessToken(accessToken);
				responseDTO.setRefreshToken(refreshToken);

				LocalDateTime expirationDate = tokenService.getExpirationDateFromJwtToken(accessToken);
				long expiresInSeconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), expirationDate);
				long expiresInHours = expiresInSeconds / 3600;
				responseDTO.setAccessTokenExpiresIn(expiresInHours + " Giờ");

				LocalDateTime expirationDateRe = tokenService.getExpirationDateFromJwtRefreshToken(refreshToken);
				long expiresInSecondsRe = ChronoUnit.SECONDS.between(LocalDateTime.now(), expirationDateRe);
				long expiresInHoursRe = expiresInSecondsRe / 3600;
				responseDTO.setRefreshTokenExpiresIn(expiresInHoursRe + " Giờ");

				UserRegisterResponseDTO userInfo = new UserRegisterResponseDTO();
				userInfo.setUserId(user.getId());
				userInfo.setEmail(user.getEmail());
				userInfo.setUserName(user.getUsername());
				userInfo.setRoles(AuthorityUtil.convertToAuthorities(user.getRolePermissions()));
				userInfo.setBio(user.getBio());
				userInfo.setGender(user.getGender());
				userInfo.setProvider(user.getProvider());
				userInfo.setProfilePictureUrl(user.getProfilePictureUrl());

				responseDTO.setUser(userInfo);
				return responseDTO;
			} else {
				throw new AppException("Invalid ID token", HttpStatus.UNAUTHORIZED.value());
			}
		} catch (Exception e) {
			throw new AppException("An error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
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

	public boolean validateNewPassword(String newPassword, String confirmNewPassword) {
		if (!newPassword.equals(confirmNewPassword)) {
			throw new AppException("New password and confirm new password must match !", 400);
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
		user.setPasswordHash(passwordEncoder.encode(newPassword));
		user.setPasswordResetToken(null);
		user.setPasswordResetExpires(null);
		userJpa.save(user);
	}

	public void save(User user) {
		userJpa.save(user);
	}

	public void logOut(User user) {
		tokenService.clearTokens(user);
	}

	@Override
	public void updateProfile(User user, String newBio, String newProfilePictureUrl) {
		// TODO Auto-generated method stub

	}
}
