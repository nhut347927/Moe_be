package com.moe.music.service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.moe.music.exception.AppException;
import com.moe.music.jpa.UserJPA;
import com.moe.music.model.User;
import com.moe.music.utility.AuthorityUtil;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class TokenService {

	private final Key key;

	private final UserJPA userJPA;

	@Value("${app.expiration}")
	private Long jwtExpirationMs;

	@Value("${app.expiration2}")
	private Long jwtExpirationMs2;

	public TokenService(UserJPA userJPA, @Value("${app.jwtSecret}") String jwtSecret) {
		this.userJPA = userJPA;
		byte[] secretBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
		if (secretBytes.length < 32) {
			throw new IllegalArgumentException("JWT secret key must be at least 32 bytes long.");
		}
		this.key = Keys.hmacShaKeyFor(secretBytes);
	}

	/**
	 * Phương thức để trích xuất tất cả claims.
	 *
	 * @param token token
	 * @return Claims
	 */
	public Claims extractAllClaims(String token) {
		return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
	}

	/**
	 * Tạo JWT Token cho người dùng.
	 *
	 * @param user Đối tượng người dùng
	 * @return Chuỗi JWT Token
	 */
	public String generateJwtToken(User user) {

		return Jwts.builder().setSubject(user.getEmail())
				.claim("roles", AuthorityUtil.convertToAuthorities(user.getRolePermissions())).setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
				.signWith(key, SignatureAlgorithm.HS256).compact();
	}

	public String generateAccessTokenFromRefreshToken(String refreshToken) {
		User user = this.getUserFromRefreshToken(refreshToken);
		if (user == null) {
			throw new UsernameNotFoundException("User not found for the given refresh token.");
		}
		return this.generateJwtToken(user);
	}

	/**
	 * Kiểm tra tính hợp lệ của token.
	 *
	 * @param token cần kiểm tra
	 * @return true nếu token hợp lệ và chưa hết hạn, ngược lại false
	 */
	public boolean validateJwtToken(String token) {
		try {
			Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();

			Date expirationDate = claims.getExpiration();

			return expirationDate.after(new Date());
		} catch (ExpiredJwtException e) {
			// Token hết hạn
			return false;
		} catch (Exception e) {
			// Các lỗi khác về token
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
		try {
			return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
		} catch (ExpiredJwtException e) {
			throw new AppException("Expired JWT token !", HttpStatus.UNAUTHORIZED.value());
		} catch (JwtException e) {
			throw new AppException("Invalid JWT token: " + e.getMessage(), HttpStatus.UNAUTHORIZED.value());
		} catch (Exception e) {

			throw new AppException("Unexpected error while parsing JWT token: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}

	/**
	 * Lấy thời gian hết hạn từ JWT token.
	 *
	 * @param token JWT token
	 * @return Thời gian hết hạn của token
	 */
	public LocalDateTime getExpirationDateFromJwtToken(String token) {
		try {
			Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();

			return claims.getExpiration().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		} catch (Exception e) {
			throw new AppException("Access Token Invalid or expired", HttpStatus.UNAUTHORIZED.value());
		}
	}

	/**
	 * Kiểm tra xem token đã hết hạn hay chưa.
	 *
	 * @param token JWT token
	 * @return true nếu token chưa hết hạn, ngược lại false
	 */
	public boolean isTokenExpired(String token) {
		try {
			LocalDateTime expirationDate = getExpirationDateFromJwtToken(token);
			return expirationDate.isBefore(LocalDateTime.now());
		} catch (Exception e) {
			System.out.println("Error checking token expiration: " + e.getMessage());
			return true;
		}
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
			user.setRefreshTokenExpires(LocalDateTime.ofInstant(
					new Date(System.currentTimeMillis() + jwtExpirationMs2).toInstant(), ZoneId.systemDefault()));
			userJPA.save(user);
			return refreshToken;
		} catch (Exception e) {
			throw new RuntimeException("Failed to generate refresh token: " + e.getMessage(), e);
		}
	}

	/**
	 * Lấy thời gian hết hạn từ JWT refresh token.
	 *
	 * @param token JWT refresh token
	 * @return Thời gian hết hạn của refresh token
	 */
	public LocalDateTime getExpirationDateFromJwtRefreshToken(String token) {
		Optional<User> user = userJPA.findByrefreshToken(token);
		if (user.isPresent()) {
			return user.get().getRefreshTokenExpires();
		} else {
			throw new AppException("Refresh Token không hợp lệ hoặc không tồn tại", HttpStatus.UNAUTHORIZED.value());
		}
	}

	/**
	 * Kiểm tra tính hợp lệ của Refresh Token.
	 *
	 * @param refreshToken Token cần kiểm tra
	 * @return true nếu token hợp lệ, ngược lại false
	 */
	public boolean validateRefreshToken(String refreshToken) {
		try {
			User user = getUserFromRefreshToken(refreshToken);
			return refreshToken.equals(user.getRefreshToken())
					&& user.getRefreshTokenExpires().isAfter(LocalDateTime.now());
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Trích xuất Refresh Token từ cookie trong yêu cầu HTTP.
	 *
	 * @param request Yêu cầu HTTP
	 * @return Refresh Token nếu tồn tại, ngược lại null
	 */
	public String extractRefreshTokenFromCookie(HttpServletRequest request) {
		if (request.getCookies() != null) {
			for (Cookie cookie : request.getCookies()) {
				if ("refresh_token".equals(cookie.getName())) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}

	/**
	 * Trích xuất Access Token từ cookie trong yêu cầu HTTP.
	 *
	 * @param request Yêu cầu HTTP
	 * @return Access Token nếu tồn tại, ngược lại null
	 */
	public String extractAccessTokenFromCookie(HttpServletRequest request) {
		if (request.getCookies() != null) {
			for (Cookie cookie : request.getCookies()) {
				if ("access_token".equals(cookie.getName())) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}

	/**
	 * Lấy đối tượng người dùng từ Refresh Token.
	 *
	 * @param refreshToken Token để xác thực người dùng
	 * @return Đối tượng người dùng nếu token hợp lệ
	 */
	public User getUserFromRefreshToken(String refreshToken) {
		Optional<User> user = userJPA.findByrefreshToken(refreshToken);
		return user.get();
	}

	/**
	 * Tạo Token Đặt lại Mật khẩu và lưu vào User.
	 *
	 * @param user Đối tượng người dùng
	 * @return Chuỗi token đặt lại mật khẩu
	 */
	public String generatePasswordResetToken(User user) {
		if (user == null) {
			throw new IllegalArgumentException("User cannot be null");
		}
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
		user.setRefreshTokenExpires(null);
		user.setPasswordResetToken(null);
		user.setPasswordResetExpires(null);
		userJPA.save(user);
	}
}
