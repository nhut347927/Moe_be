package com.moe.music.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import com.moe.music.exception.AppException;
import com.moe.music.jpa.UserJPA;
import com.moe.music.model.User;
import com.moe.music.response.ResponseAPI;
import com.moe.music.service.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	@Autowired
	private UserJPA userJPA;

	@Autowired
	private UserService userService;

	@PostMapping("/register")
	public ResponseEntity<ResponseAPI<User>> register(@RequestBody RegisterRequestDTO request) {
		ResponseAPI<User> response = new ResponseAPI<>();
		try {
			User registeredUser = userService.register(request);
			response.setCode(200);
			response.setMessage("Registration successful");
			response.setData(registeredUser);
			return ResponseEntity.status(HttpStatus.OK).body(response);
		} catch (AppException e) {
			// Bắt AppException để trả về mã trạng thái và thông điệp cụ thể
			response.setCode(e.getStatusCode());
			response.setMessage(e.getMessage());
			response.setData(null);
			return ResponseEntity.status(e.getStatusCode()).body(response);
		} catch (Exception e) {
			// Bắt các loại ngoại lệ khác và trả về mã lỗi chung
			response.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setMessage("An error occurred: " + e.getMessage());
			response.setData(null);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@PostMapping("/login")
	public ResponseEntity<ResponseAPI<LoginResponseDTO>> login(@RequestBody LoginRequestDTO request) {
		ResponseAPI<LoginResponseDTO> response = new ResponseAPI<>();
		try {
			LoginResponseDTO login = userService.login(request);
			response.setCode(200);
			response.setMessage("Login successful");
			response.setData(login);
			return ResponseEntity.ok(response);
		} catch (AppException e) {
			// Bắt AppException để trả về mã trạng thái và thông điệp cụ thể
			response.setCode(e.getStatusCode());
			response.setMessage(e.getMessage());
			response.setData(null);
			return ResponseEntity.status(e.getStatusCode()).body(response);
		} catch (Exception e) {
			// Bắt các loại ngoại lệ khác và trả về mã lỗi chung
			response.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setMessage("An error occurred: " + e.getMessage());
			response.setData(null);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@PutMapping("/change-password")
	public ResponseEntity<ResponseAPI<Void>> changePassword(@AuthenticationPrincipal User user,
			@RequestBody ChangePasswordRequestDTO request) {
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

}
