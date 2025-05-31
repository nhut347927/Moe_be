package com.moe.socialnetwork.auth.services.impl;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.moe.socialnetwork.auth.dtos.LoginRequestDTO;
import com.moe.socialnetwork.auth.dtos.LoginResponseDTO;
import com.moe.socialnetwork.auth.dtos.RegisterRequestDTO;
import com.moe.socialnetwork.auth.dtos.UserRegisterResponseDTO;
import com.moe.socialnetwork.auth.services.IAuthService;
import com.moe.socialnetwork.auth.services.ITokenService;
import com.moe.socialnetwork.common.jpa.UserJpa;
import com.moe.socialnetwork.common.models.User;
import com.moe.socialnetwork.exception.AppException;
import com.moe.socialnetwork.util.AuthorityUtil;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class AuthServiceImpl implements IAuthService {

    private final UserJpa userJpa;
    private final PasswordEncoder passwordEncoder;
    private final ITokenService tokenService;
    @Value("${google.client.id}")
    private String googleClientId;

    public AuthServiceImpl(UserJpa userJPA, PasswordEncoder passwordEncoder, TokenServiceImpl tokenService) {
        this.userJpa = userJPA;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Transactional
    public UserRegisterResponseDTO register(RegisterRequestDTO request) {
        String email = request.getEmail().trim().toLowerCase();

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new AppException("Password and confirm password must match", HttpStatus.BAD_REQUEST.value());
        }

        if (userJpa.findByEmail(email).isPresent()) {
            throw new AppException("Email already exists", HttpStatus.CONFLICT.value());
        }

        String baseName = email.split("@")[0];
        String uniqueUsername = baseName + System.currentTimeMillis();

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setDisplayName(request.getDisplayName());
        user.setUserName(uniqueUsername);
        
        try {
            User savedUser = userJpa.save(user);
            return buildUserRegisterResponse(savedUser);
        } catch (DataIntegrityViolationException e) {
            throw new AppException("Database constraint error: " + e.getMessage(), HttpStatus.CONFLICT.value());
        } catch (Exception e) {
            throw new AppException("Registration failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        String email = request.getEmail();
        String password = request.getPassword();

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new AppException("Email and password must not be empty", HttpStatus.BAD_REQUEST.value());
        }

        User user = userJpa.findByEmail(email)
                .orElseThrow(() -> new AppException("Email is not registered", HttpStatus.NOT_FOUND.value()));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new AppException("Email or password is incorrect", HttpStatus.UNAUTHORIZED.value());
        }

        return buildLoginResponse(user);
    }

    public LoginResponseDTO loginWithGoogle(String token) {
        HttpTransport transport = new NetHttpTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(Collections.singletonList(googleClientId)).build();

        try {
            GoogleIdToken idToken = verifier.verify(token);
            if (idToken == null) {
                throw new AppException("Invalid Google ID token", HttpStatus.UNAUTHORIZED.value());
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");

            User user = userJpa.findByEmail(email).orElseGet(() -> {
                String baseName = email.split("@")[0];
                String uniqueUsername = baseName + System.currentTimeMillis();
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setPasswordHash(null);
                newUser.setDisplayName(name);
                newUser.setUserName(uniqueUsername);
                newUser.setAvatar(pictureUrl);
                newUser.setProvider("GOOGLE");
                return userJpa.save(newUser);
            });

            if (!user.getProvider().equals("GOOGLE")) {
                throw new AppException("Email already registered with a different provider", HttpStatus.CONFLICT.value());
            }

            return buildLoginResponse(user);
        } catch (Exception e) {
            throw new AppException("Google login failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Transactional
    public void changePassword(User user, String newPassword) {
        if (newPassword == null || newPassword.trim().isBlank()) {
            throw new AppException("New password cannot be empty", HttpStatus.BAD_REQUEST.value());
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        try {
            userJpa.save(user);
        } catch (Exception e) {
            throw new AppException("Failed to change password: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    public boolean validateNewPassword(String newPassword, String confirmNewPassword) {
        if (!newPassword.equals(confirmNewPassword)) {
            throw new AppException("New password and confirm password must match", HttpStatus.BAD_REQUEST.value());
        }
        return true;
    }

    public User findByEmail(String email) {
        return userJpa.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User with email " + email + " not found"));
    }

    public User findByResetToken(String token) {
        if (token == null || token.isBlank()) {
            throw new AppException("Reset token cannot be null or empty", HttpStatus.BAD_REQUEST.value());
        }
        return userJpa.findByPasswordResetToken(token)
                .orElseThrow(() -> new AppException("Invalid or expired reset token", HttpStatus.BAD_REQUEST.value()));
    }

    @Transactional
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

    @Transactional
    public void updateProfile(User user, String newBio, String newProfilePictureUrl) {
        if (newBio != null) {
            user.setBio(newBio.trim());
        }
        if (newProfilePictureUrl != null) {
            user.setAvatar(newProfilePictureUrl.trim());
        }
        userJpa.save(user);
    }

    private LoginResponseDTO buildLoginResponse(User user) {
        try {
            String refreshToken = tokenService.generateRefreshToken(user);
            String accessToken = tokenService.generateJwtToken(user);

            LoginResponseDTO responseDTO = new LoginResponseDTO();
            responseDTO.setAccessToken(accessToken);
            responseDTO.setRefreshToken(refreshToken);

            LocalDateTime expirationDate = tokenService.getExpirationDateFromJwtToken(accessToken);
            long expiresInSeconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), expirationDate);
            responseDTO.setAccessTokenExpiresIn(expiresInSeconds / 3600 + " Giờ");

            // Use validateJwtToken for refresh token since they are now similar
            LocalDateTime expirationDateRe = tokenService.getExpirationDateFromJwtToken(refreshToken);
            long expiresInSecondsRe = ChronoUnit.SECONDS.between(LocalDateTime.now(), expirationDateRe);
            responseDTO.setRefreshTokenExpiresIn(expiresInSecondsRe / 3600 + " Giờ");

            UserRegisterResponseDTO userInfo = buildUserRegisterResponse(user);
            responseDTO.setUser(userInfo);
            return responseDTO;
        } catch (Exception e) {
            throw new AppException("Failed to generate tokens: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private UserRegisterResponseDTO buildUserRegisterResponse(User user) {
        UserRegisterResponseDTO userInfo = new UserRegisterResponseDTO();
        userInfo.setUserId(user.getId());
        userInfo.setEmail(user.getEmail());
        userInfo.setUserName(user.getUsername());
        userInfo.setRoles(AuthorityUtil.convertToAuthorities(user.getRolePermissions()));
        userInfo.setBio(user.getBio());
        userInfo.setGender(user.getGender());
        userInfo.setProvider(user.getProvider());
        userInfo.setProfilePictureUrl(user.getAvatar());
        return userInfo;
    }

     public String refreshAccessToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AppException("Refresh token must not be null or empty", HttpStatus.BAD_REQUEST.value());
        }
        if (!tokenService.validateJwtToken(refreshToken)) {
            throw new AppException("Invalid or expired refresh token", HttpStatus.UNAUTHORIZED.value());
        }
        String email = tokenService.getEmailFromJwtToken(refreshToken);
        User user = userJpa.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found for email: " + email));
        return tokenService.generateJwtToken(user);
    }
}