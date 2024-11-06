package com.moe.music.utility;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.moe.music.exception.AppException;
import com.moe.music.jpa.UserJPA;
import com.moe.music.model.User;
import com.moe.music.service.TokenService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

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
	public User getUserFromAuthHeader(String authHeader, HttpServletResponse response) throws AppException {

		if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
			throw new AppException("Invalid token. Please provide a valid token.", HttpStatus.UNAUTHORIZED.value());
		}

		String token = authHeader.substring(7);

		String email = tokenService.getEmailFromJwtToken(token);
		User user = userJPA.findByEmail(email);

		if (tokenService.validateJwtToken(token)) {
			if (user == null) {
				throw new AppException("User not found with email: " + email, HttpStatus.NOT_FOUND.value());
			}
			return user;
		} else {
			if (user == null) {
				throw new AppException("User not found with email: " + email, HttpStatus.NOT_FOUND.value());
			}

			String refreshToken = user.getRefreshToken();
			if (refreshToken != null && tokenService.validateRefreshToken(user, refreshToken)) {

				String newAccessToken = tokenService.generateJwtToken(user);

				Cookie newAccessCookie = new Cookie("accessToken", newAccessToken);
				newAccessCookie.setHttpOnly(true);
				newAccessCookie.setPath("/");
				// Đặt thời hạn 1 năm cho cookie (365 * 24 * 60 * 60 giây)
				newAccessCookie.setMaxAge(365 * 24 * 60 * 60);
				response.addCookie(newAccessCookie);

				return user;
			} else {
				throw new AppException(
						"Both access token and refresh token are invalid or expired. Please log in again.",
						HttpStatus.UNAUTHORIZED.value());
			}
		}
	}

	public List<String> extractPermissions(String token) {
		Claims claims = tokenService.extractAllClaims(token);
		return claims.get("permissions", List.class);
	}

}
