package com.moe.music.service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.moe.music.jpa.UserJPA;
import com.moe.music.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class TokenService {

	private final Key key;
	private final UserJPA userJPA;

	@Value("${app.expiration}")
	private int jwtExpirationMs;

	@Autowired
	public TokenService(UserJPA userJPA, @Value("${app.jwtSecret}") String jwtSecret) {
		this.userJPA = userJPA;
		byte[] secretBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
		if (secretBytes.length < 32) {
			throw new IllegalArgumentException("JWT secret key must be at least 32 bytes long.");
		}
		this.key = Keys.hmacShaKeyFor(secretBytes);
	}

	/**
	 * Tạo JWT Token cho người dùng.
	 *
	 * @param user Đối tượng người dùng
	 * @return Chuỗi JWT Token
	 */
	public String generateJwtToken(User user) {
		Set<String> permissions;

		if ("GUEST".equalsIgnoreCase(user.getRole().getRoleName())) {
			permissions = Collections.emptySet();
		} else {
			permissions = user.getRole().getRolePermission().stream()
					.map(rolePermission -> rolePermission.getPermission().getActionName()).collect(Collectors.toSet());
		}

		return Jwts.builder().setSubject(user.getEmail()).claim("permissions", permissions).setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
				.signWith(key, SignatureAlgorithm.HS256).compact();
	}

	/**
	 * Kiểm tra tính hợp lệ của token.
	 *
	 * @param token token cần kiểm tra
	 * @return true nếu token hợp lệ và chưa hết hạn, ngược lại false
	 */
	public boolean validateJwtToken(String token) {
		try {
			Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Lấy tên người dùng từ JWT token.
	 *
	 * @param token JWT token
	 * @return Tên người dùng từ token hoặc null nếu token không hợp lệ
	 */
	public String getEmailFromJwtToken(String token) {
		return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
	}

	/**
	 * Lấy thời gian hết hạn từ JWT token.
	 *
	 * @param token JWT token
	 * @return Thời gian hết hạn của token
	 */
	public Date getExpirationDateFromJwtToken(String token) {
		Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
		return claims.getExpiration();
	}

	/**
	 * Kiểm tra xem token đã hết hạn hay chưa.
	 *
	 * @param token JWT token
	 * @return true nếu token chưa hết hạn, ngược lại false
	 */
	public boolean isTokenExpired(String token) {
		return getExpirationDateFromJwtToken(token).before(new Date());
	}

	/**
	 * Tạo Refresh Token và lưu vào User.
	 *
	 * @param user Đối tượng người dùng
	 * @return Chuỗi Refresh Token
	 */
	public String generateRefreshToken(User user) {
		try {
			String refreshToken = UUID.randomUUID().toString();
			user.setRefreshToken(refreshToken);
			user.setRefreshTokenExpires(LocalDateTime.now().plusDays(90)); // Thời hạn 90 ngày
			userJPA.save(user);
			return refreshToken;
		} catch (Exception e) {
			throw new RuntimeException("Failed to generate refresh token: " + e.getMessage(), e);
		}
	}

	/**
	 * Kiểm tra Refresh Token có hợp lệ với người dùng hay không.
	 *
	 * @param user Đối tượng người dùng
	 * @return true nếu refresh token hợp lệ, ngược lại false
	 */
	public boolean isRefreshTokenExpired(User user) {
		return user.getRefreshTokenExpires().isBefore(LocalDateTime.now());
	}

	/**
	 * Kiểm tra Refresh Token có hợp lệ với người dùng hay không.
	 *
	 * @param user         Đối tượng người dùng
	 * @param refreshToken Token cần kiểm tra
	 * @return true nếu refresh token hợp lệ, ngược lại false
	 */
	public boolean validateRefreshToken(User user, String refreshToken) {
		return refreshToken.equals(user.getRefreshToken()) && !isRefreshTokenExpired(user);
	}

	/**
	 * Tạo Token Đặt lại Mật khẩu và lưu vào User.
	 *
	 * @param user Đối tượng người dùng
	 * @return Chuỗi token đặt lại mật khẩu
	 */
	public String generatePasswordResetToken(User user) {
		String resetToken = UUID.randomUUID().toString();
		user.setPasswordResetToken(resetToken);
		user.setPasswordResetExpires(LocalDateTime.now().plusHours(1));
		userJPA.save(user);
		return resetToken;
	}

	/**
	 * Kiểm tra tính hợp lệ của token đặt lại mật khẩu.
	 *
	 * @param user  Đối tượng người dùng
	 * @param token Token đặt lại mật khẩu
	 * @return true nếu token hợp lệ và chưa hết hạn, ngược lại false
	 */
	public boolean validatePasswordResetToken(User user, String token) {
		return token.equals(user.getPasswordResetToken())
				&& user.getPasswordResetExpires().isAfter(LocalDateTime.now());
	}

	/**
	 * Xóa Refresh và Password Reset Token của người dùng.
	 *
	 * @param user Đối tượng người dùng
	 */
	public void clearTokens(User user) {
		user.setRefreshToken(null);
		user.setPasswordResetToken(null);
		user.setPasswordResetExpires(null);
		userJPA.save(user);
	}
}
