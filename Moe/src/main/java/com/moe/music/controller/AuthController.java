package com.moe.music.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.moe.music.dtoauth.ChangePasswordRequest;
import com.moe.music.dtoauth.LoginRequest;
import com.moe.music.dtoauth.RegisterRequest;
import com.moe.music.model.User;
import com.moe.music.service.UserService;
import com.moe.music.response.ResponseAPI;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	@Autowired
	private UserService userService;

	@PostMapping("/register")
	public ResponseEntity<ResponseAPI<User>> register(@RequestBody RegisterRequest request) {
		ResponseAPI<User> response = new ResponseAPI<>();
		try {
			User registeredUser = userService.register(request);
			response.setCode(201);
			response.setMessage("Đăng ký thành công");
			response.setData(registeredUser);
			return ResponseEntity.status(201).body(response);
		} catch (Exception e) {
			response.setCode(500);
			response.setMessage("Đã xảy ra lỗi: " + e.getMessage());
			response.setData(null);
			return ResponseEntity.status(500).body(response);
		}
	}

	@PostMapping("/login")
	public ResponseEntity<ResponseAPI<String>> login(@RequestBody LoginRequest request) {
		ResponseAPI<String> response = new ResponseAPI<>();
		try {
			String token = userService.login(request);
			response.setCode(200);
			response.setMessage("Đăng nhập thành công");
			response.setData(token);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			response.setCode(500);
			response.setMessage("Đã xảy ra lỗi: " + e.getMessage());
			response.setData(null);
			return ResponseEntity.status(500).body(response);
		}
	}

	@PutMapping("/change-password")
	public ResponseEntity<ResponseAPI<Void>> changePassword(@AuthenticationPrincipal User user,
			@RequestBody ChangePasswordRequest request) {
		ResponseAPI<Void> response = new ResponseAPI<>();
		try {

			if (!userService.validateOldPassword(user, request.getOldPassword())) {
				response.setCode(400); // 400 Bad Request
				response.setMessage("Mật khẩu cũ không chính xác");
				return ResponseEntity.badRequest().body(response);
			}

			userService.changePassword(user, request.getNewPassword());
			response.setCode(200);
			response.setMessage("Thay đổi mật khẩu thành công");
			response.setData(null);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			response.setCode(500);
			response.setMessage("Đã xảy ra lỗi: " + e.getMessage());
			response.setData(null);
			return ResponseEntity.status(500).body(response);
		}
	}
}
