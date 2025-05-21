package com.moe.socialnetwork.auth.services;

import com.moe.socialnetwork.auth.dtos.LoginRequestDTO;
import com.moe.socialnetwork.auth.dtos.LoginResponseDTO;
import com.moe.socialnetwork.auth.dtos.RegisterRequestDTO;
import com.moe.socialnetwork.auth.dtos.UserRegisterResponseDTO;
import com.moe.socialnetwork.common.models.User;

public interface IAuthService {

	UserRegisterResponseDTO register(RegisterRequestDTO request);

	LoginResponseDTO login(LoginRequestDTO request);

	LoginResponseDTO loginWithGoogle(String token);

	void changePassword(User user, String newPassword);

	void updateProfile(User user, String newBio, String newProfilePictureUrl);

	void logOut(User user);

	boolean validateNewPassword(String newPassword, String confirmNewPassword);

	User findByEmail(String email);

	User findByResetToken(String token);

	void updatePassword(User user, String newPassword);

	String refreshAccessToken(String refreshToken);
}
