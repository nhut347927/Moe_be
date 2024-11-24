package com.moe.music.controller;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.moe.music.dtoauth.ChangePasswordRequestDTO;
import com.moe.music.dtoauth.LoginRequestDTO;
import com.moe.music.dtoauth.LoginResponseDTO;
import com.moe.music.dtoauth.RegisterRequestDTO;
import com.moe.music.dtoauth.RequestPasswordResetRequestDTO;
import com.moe.music.dtoauth.ResetPasswordRequestDTO;
import com.moe.music.dtoauth.UserRegisterResponseDTO;
import com.moe.music.exception.AppException;
import com.moe.music.model.User;
import com.moe.music.response.ResponseAPI;
import com.moe.music.service.EmailService;
import com.moe.music.service.TokenService;
import com.moe.music.service.UserService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	@Autowired
	private UserService userService;

	@Autowired
	private TokenService tokenService;

	@Autowired
	private EmailService emailService;

	@PostMapping("/register")
	public ResponseEntity<ResponseAPI<UserRegisterResponseDTO>> register(
			@RequestBody @Valid RegisterRequestDTO request) {
		ResponseAPI<UserRegisterResponseDTO> response = new ResponseAPI<>();
		try {
			UserRegisterResponseDTO registeredUser = userService.register(request);
			response.setCode(200);
			response.setMessage("Registration successful");
			response.setData(registeredUser);
			return ResponseEntity.status(HttpStatus.OK).body(response);
		} catch (AppException e) {
			response.setCode(e.getStatusCode());
			response.setMessage(e.getMessage());
			response.setData(null);
			return ResponseEntity.status(e.getStatusCode()).body(response);
		} catch (Exception e) {
			response.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setMessage("An error occurred: " + e.getMessage());
			response.setData(null);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@PostMapping("/login")
	public ResponseEntity<ResponseAPI<LoginResponseDTO>> login(@RequestBody @Valid LoginRequestDTO request) {
		ResponseAPI<LoginResponseDTO> response = new ResponseAPI<>();
		try {

			LoginResponseDTO login = userService.login(request);

			ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", login.getRefreshToken()).httpOnly(true)
					.secure(true).path("/").sameSite("Strict").maxAge(Duration.ofDays(30)).build();

			response.setCode(200);
			response.setMessage("Login successful");
			response.setData(login);

			return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, refreshCookie.toString()).body(response);
		} catch (AppException e) {
			response.setCode(e.getStatusCode());
			response.setMessage(e.getMessage());
			response.setData(null);
			return ResponseEntity.status(e.getStatusCode()).body(response);
		} catch (Exception e) {
			response.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setMessage("An error occurred: " + e.getMessage());
			response.setData(null);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@PutMapping("/change-password")
	public ResponseEntity<ResponseAPI<Void>> changePassword(@AuthenticationPrincipal User user,
			@RequestBody @Valid ChangePasswordRequestDTO request) {
		ResponseAPI<Void> response = new ResponseAPI<>();
		try {

			if (user == null) {
				response.setCode(HttpStatus.UNAUTHORIZED.value());
				response.setMessage("User is not authenticated");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
			}

			if (!userService.validateOldPassword(user, request.getOldPassword())) {
				response.setCode(HttpStatus.BAD_REQUEST.value());
				response.setMessage("Old password is incorrect");
				return ResponseEntity.badRequest().body(response);
			}

			userService.changePassword(user, request.getNewPassword());
			response.setCode(HttpStatus.OK.value());
			response.setMessage("Password changed successfully");
			return ResponseEntity.ok(response);
		} catch (AppException e) {
			response.setCode(e.getStatusCode());
			response.setMessage(e.getMessage());
			return ResponseEntity.status(e.getStatusCode()).body(response);
		} catch (Exception e) {
			response.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setMessage("An error occurred: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@PostMapping("/request-password-reset")
	public ResponseEntity<ResponseAPI<String>> requestPasswordReset(
			@RequestBody @Valid RequestPasswordResetRequestDTO request) {
		ResponseAPI<String> response = new ResponseAPI<>();
		String email = request.getEmail();

		try {
			User user = userService.findByEmail(email);

			String resetToken = tokenService.generatePasswordResetToken(user);
			emailService.sendPasswordResetEmail(user.getEmail(), resetToken);

			response.setCode(HttpStatus.OK.value());
			response.setMessage("Password reset email sent successfully");
			response.setData("Success");
			return ResponseEntity.ok(response);

		} catch (IllegalArgumentException ex) {
			response.setCode(HttpStatus.BAD_REQUEST.value());
			response.setMessage("Email cannot be null or empty");
			response.setData(null);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

		} catch (EntityNotFoundException ex) {
			response.setCode(HttpStatus.NOT_FOUND.value());
			response.setMessage("User with email " + email + " not found");
			response.setData(null);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

		} catch (Exception ex) {
			response.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setMessage("An error occurred while processing the request");
			response.setData(null);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@PostMapping("/reset-password")
	public ResponseEntity<ResponseAPI<String>> resetPassword(@RequestBody @Valid ResetPasswordRequestDTO request) {
		ResponseAPI<String> response = new ResponseAPI<>();

		try {
			String token = request.getToken();
			String newPassword = request.getNewPassword();

			if (newPassword == null || newPassword.isEmpty()) {
				response.setCode(HttpStatus.BAD_REQUEST.value());
				response.setMessage("New password cannot be null or empty");
				response.setData(null);
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
			}

			User user = userService.findByResetToken(token);

			if (!tokenService.validatePasswordResetToken(user, token)) {
				response.setCode(HttpStatus.UNAUTHORIZED.value());
				response.setMessage("Invalid or expired token");
				response.setData(null);
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
			}

			userService.updatePassword(user, newPassword);
			user.setPasswordResetToken(null);
			user.setPasswordResetExpires(null);
			userService.save(user);

			response.setCode(HttpStatus.OK.value());
			response.setMessage("Password has been reset successfully");
			response.setData("Success");
			return ResponseEntity.ok(response);

		} catch (IllegalArgumentException ex) {
			response.setCode(HttpStatus.BAD_REQUEST.value());
			response.setMessage(ex.getMessage());
			response.setData(null);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

		} catch (EntityNotFoundException ex) {
			response.setCode(HttpStatus.NOT_FOUND.value());
			response.setMessage(ex.getMessage());
			response.setData(null);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

		} catch (Exception ex) {
			response.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setMessage("An error occurred while processing the password reset");
			response.setData(null);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@PostMapping("/refresh-token")
	public ResponseEntity<ResponseAPI<String>> refreshAccessToken(HttpServletRequest request) {
		ResponseAPI<String> response = new ResponseAPI<>();
		try {

			String refreshToken = tokenService.extractTokenFromCookie(request);

			if (refreshToken == null || !tokenService.validateRefreshToken(refreshToken)) {
				response.setCode(HttpStatus.UNAUTHORIZED.value());
				response.setMessage("Invalid or expired refresh token");
				response.setData(null);
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
			}

			User user = tokenService.getUserFromRefreshToken(refreshToken);

			String newAccessToken = tokenService.generateJwtToken(user);

			response.setCode(HttpStatus.OK.value());
			response.setMessage("Access token refreshed successfully");
			response.setData(newAccessToken);
			return ResponseEntity.ok(response);

		} catch (AppException e) {

			response.setCode(e.getStatusCode());
			response.setMessage(e.getMessage());
			response.setData(null);
			return ResponseEntity.status(e.getStatusCode()).body(response);
		} catch (Exception e) {

			response.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setMessage("An error occurred during token refresh: " + e.getMessage());
			response.setData(null);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@PostMapping("/logout")
	public ResponseEntity<ResponseAPI<String>> logout(@AuthenticationPrincipal User user) {
		ResponseAPI<String> response = new ResponseAPI<>();

		try {

			userService.logOut(user);

			ResponseCookie deleteCookie = ResponseCookie.from("refresh_token", "").httpOnly(true).secure(true).path("/")
					.sameSite("Strict").maxAge(0).build();

			response.setCode(HttpStatus.OK.value());
			response.setMessage("Logged out successfully");
			response.setData("Success");

			return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, deleteCookie.toString()).body(response);

		} catch (AppException e) {

			response.setCode(e.getStatusCode());
			response.setMessage(e.getMessage());
			response.setData(null);
			return ResponseEntity.status(e.getStatusCode()).body(response);

		} catch (Exception e) {
			response.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setMessage("An error occurred during logout: " + e.getMessage());
			response.setData(null);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

}
