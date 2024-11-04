package com.moe.music.controller;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.moe.music.model.User;
import com.moe.music.response.ResponseAPI;
import com.moe.music.service.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

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
			return ResponseEntity.status(200).body(response);
		} catch (Exception e) {
			response.setCode(500);
			response.setMessage("An error occurred: " + e.getMessage());
			response.setData(null);
			return ResponseEntity.status(500).body(response);
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
		} catch (Exception e) {
			response.setCode(500);
			response.setMessage("An error occurred: " + e.getMessage());
			response.setData(null);
			return ResponseEntity.status(500).body(response);
		}
	}

	@PutMapping("/change-password")
	public ResponseEntity<ResponseAPI<Void>> changePassword(@AuthenticationPrincipal User user,
			@RequestBody ChangePasswordRequestDTO request) {
		ResponseAPI<Void> response = new ResponseAPI<>();
		try {

			if (!userService.validateOldPassword(user, request.getOldPassword())) {
				response.setCode(400); // 400 Bad Request
				response.setMessage("Old password is incorrect");
				return ResponseEntity.badRequest().body(response);
			}

			userService.changePassword(user, request.getNewPassword());
			response.setCode(200);
			response.setMessage("Password changed successfully");
			response.setData(null);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			response.setCode(500);
			response.setMessage("An error occurred: " + e.getMessage());
			response.setData(null);
			return ResponseEntity.status(500).body(response);
		}
	}
}
