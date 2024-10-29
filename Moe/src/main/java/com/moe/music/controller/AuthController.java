package com.moe.music.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.moe.music.dtoauth.ChangePasswordRequest;
import com.moe.music.dtoauth.LoginRequest;
import com.moe.music.dtoauth.RegisterRequest;
import com.moe.music.model.User;
import com.moe.music.service.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(userService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        String token = userService.login(request);
        return ResponseEntity.ok(token);
    }

    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal User user, @RequestBody ChangePasswordRequest request) {
        // Kiểm tra mật khẩu cũ trước khi thay đổi
        if (userService.validateOldPassword(user, request.getOldPassword())) {
            userService.changePassword(user, request.getNewPassword());
            return ResponseEntity.ok().build();
        } else {
            throw new RuntimeException("Old password is incorrect");
        }
    }
}
