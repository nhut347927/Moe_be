package com.moe.music.utility;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.moe.music.exception.AppException;
import com.moe.music.jpa.UserJPA;
import com.moe.music.model.User;
import com.moe.music.service.TokenService;

@Component
public class JwtUtil {

	private final TokenService tokenService;
	private final UserJPA userJPA;

	@Autowired
	public JwtUtil(TokenService tokenService, UserJPA userJPA) {
		this.tokenService = tokenService;
		this.userJPA = userJPA;
	}

	/**
	 * Extracts and verifies the token from the authHeader.
	 *
	 * @param authHeader Header containing the token
	 * @return User object associated with the token
	 * @throws AppException if there is an error during authentication
	 */
	public User getUserFromAuthHeader(String authHeader) throws AppException {

		if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
			throw new AppException("Invalid token. Please provide a valid token.", HttpStatus.UNAUTHORIZED.value());
		}

		String token = authHeader.substring(7);

		// Get email from token
		String email = tokenService.getEmailFromJwtToken(token);
		User user = userJPA.findByEmail(email);

		// Validate access token
		if (tokenService.validateJwtToken(token)) {
			if (user == null) {
				throw new AppException("User not found with email: " + email, HttpStatus.NOT_FOUND.value());
			}
			return user;
		} else {
			// Access token is expired, check refresh token
			if (user == null) {
				throw new AppException("User not found with email: " + email, HttpStatus.NOT_FOUND.value());
			}

			String refreshToken = user.getRefreshToken();
			if (refreshToken != null && tokenService.validateRefreshToken(user, refreshToken)) {
				// Refresh token is valid, generate new access token
				String newAccessToken = tokenService.generateJwtToken(user);
				// Optionally, update the user's refresh token here if needed
				return user; // Return the user object or an updated token response as needed
			} else {
				// Both access token and refresh token are invalid
				// Redirect to login
				throw new AppException(
						"Both access token and refresh token are invalid or expired. Please log in again.",
						HttpStatus.UNAUTHORIZED.value());
			}
		}
	}
}
