package com.moe.socialnetwork.auth.controllers;

import org.springframework.beans.factory.annotation.Value;
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

import com.moe.socialnetwork.api.services.IEmailService;
import com.moe.socialnetwork.auth.dtos.ChangePasswordRequestDTO;
import com.moe.socialnetwork.auth.dtos.LoginRequestDTO;
import com.moe.socialnetwork.auth.dtos.LoginResponseDTO;
import com.moe.socialnetwork.auth.dtos.LoginWithGoogleRequestDTO;
import com.moe.socialnetwork.auth.dtos.RegisterRequestDTO;
import com.moe.socialnetwork.auth.dtos.PasswordResetRequestRequestDTO;
import com.moe.socialnetwork.auth.dtos.PasswordResetRequestDTO;
import com.moe.socialnetwork.auth.dtos.UserRegisterResponseDTO;
import com.moe.socialnetwork.auth.services.IAuthService;
import com.moe.socialnetwork.auth.services.ITokenService;
import com.moe.socialnetwork.common.models.User;
import com.moe.socialnetwork.common.response.ResponseAPI;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final IAuthService authService;
    private final ITokenService tokenService;
    private final IEmailService emailService;

    @Value("${app.expiration24h}")
    private Long jwtExpirationMs;

    @Value("${app.expiration6months}")
    private Long jwtExpirationMs2;

    public AuthController(IAuthService authService, ITokenService tokenService, IEmailService emailService) {
        this.authService = authService;
        this.tokenService = tokenService;
        this.emailService = emailService;
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseAPI<UserRegisterResponseDTO>> register(@RequestBody @Valid RegisterRequestDTO request) {
        UserRegisterResponseDTO registeredUser = authService.register(request);
        return ResponseEntity.ok(ResponseAPI.of(HttpStatus.OK.value(), "Registration successful", registeredUser));
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseAPI<LoginResponseDTO>> login(@RequestBody @Valid LoginRequestDTO request) {
        LoginResponseDTO login = authService.login(request);
        return buildLoginResponse(login);
    }

    @PostMapping("/google-login")
    public ResponseEntity<ResponseAPI<LoginResponseDTO>> loginWithGoogle(@RequestBody @Valid LoginWithGoogleRequestDTO request) {
        LoginResponseDTO login = authService.loginWithGoogle(request.getToken());
        return buildLoginResponse(login);
    }

    @PutMapping("/change-password")
    public ResponseEntity<ResponseAPI<String>> changePassword(@AuthenticationPrincipal User user,
                                                            @RequestBody @Valid ChangePasswordRequestDTO request) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ResponseAPI.error(HttpStatus.UNAUTHORIZED.value(), "User is not authenticated", null));
        }

        authService.validateNewPassword(request.getNewPassword(), request.getConfirmNewPassword());
        authService.changePassword(user, request.getNewPassword());

        return ResponseEntity.ok(ResponseAPI.of(HttpStatus.OK.value(), "Password changed successfully",
                "Password updated for user: " + user.getUsername()));
    }

    @PostMapping("/password-reset-request")
    public ResponseEntity<ResponseAPI<String>> passwordResetRequest(@RequestBody @Valid PasswordResetRequestRequestDTO request) {
        User user = authService.findByEmail(request.getEmail());
        String resetToken = tokenService.generatePasswordResetToken(user);
        emailService.sendPasswordResetEmail(user.getEmail(), resetToken);

        return ResponseEntity.ok(ResponseAPI.of(HttpStatus.OK.value(), 
                "Password reset email sent successfully. Please check your email!", "Success"));
    }

    @PostMapping("/password-reset")
    public ResponseEntity<ResponseAPI<String>> passwordReset(@RequestBody @Valid PasswordResetRequestDTO request) {
        User user = authService.findByResetToken(request.getToken());

        if (!tokenService.validatePasswordResetToken(user, request.getToken())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ResponseAPI.error(HttpStatus.UNAUTHORIZED.value(), "Invalid or expired reset token", null));
        }

        authService.validateNewPassword(request.getNewPassword(), request.getConfirmNewPassword());
        authService.updatePassword(user, request.getNewPassword());

        return ResponseEntity.ok(ResponseAPI.of(HttpStatus.OK.value(), "Password reset successfully", "Success"));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ResponseAPI<String>> refreshAccessToken(HttpServletRequest request) {
        String refreshToken = tokenService.extractRefreshTokenFromCookie(request);

        if (refreshToken == null || !tokenService.validateJwtToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ResponseAPI.error(HttpStatus.UNAUTHORIZED.value(), "Invalid or expired refresh token", null));
        }

        String email = tokenService.getEmailFromJwtToken(refreshToken);
        User user = authService.findByEmail(email);
        String newAccessToken = tokenService.generateJwtToken(user);

        return ResponseEntity.ok(ResponseAPI.of(HttpStatus.OK.value(), "Access token refreshed successfully", newAccessToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<ResponseAPI<String>> logout(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ResponseAPI.error(HttpStatus.UNAUTHORIZED.value(), "User is not authenticated", null));
        }

        authService.logOut(user);
        ResponseCookie deleteRefreshCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Strict")
                .maxAge(0)
                .build();

        ResponseCookie deleteAccessCookie = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Strict")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteRefreshCookie.toString(), deleteAccessCookie.toString())
                .body(ResponseAPI.of(HttpStatus.OK.value(), "Logged out successfully", "Success"));
    }

    private ResponseEntity<ResponseAPI<LoginResponseDTO>> buildLoginResponse(LoginResponseDTO login) {
        int maxAgeAccessToken = (int) (jwtExpirationMs / 1000);
        int maxAgeRefreshToken = (int) (jwtExpirationMs2 / 1000);

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", login.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(maxAgeRefreshToken)
                .sameSite("Lax")
                .build();

        ResponseCookie accessCookie = ResponseCookie.from("access_token", login.getAccessToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(maxAgeAccessToken)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString(), accessCookie.toString())
                .body(ResponseAPI.of(HttpStatus.OK.value(), "Login successful", login));
    }
}