package com.moe.music.interfaces;

import com.moe.music.dto.LoginRequestDTO;
import com.moe.music.dto.LoginResponseDTO;
import com.moe.music.dto.RegisterRequestDTO;
import com.moe.music.dto.UserRegisterResponseDTO;
import com.moe.music.model.User;

public interface UserInterface {

	UserRegisterResponseDTO register(RegisterRequestDTO request);

	LoginResponseDTO login(LoginRequestDTO request);

	LoginResponseDTO loginWithGoogle(String token);

	void changePassword(User user, String newPassword);

	void updateProfile(User user, String newBio, String newProfilePictureUrl);

	void logOut(User user);

	User findByEmail(String email);

	User findByResetToken(String token);

	void updatePassword(User user, String newPassword);
}
