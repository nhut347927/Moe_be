package com.moe.music.service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.moe.music.jpa.UserJPA;
import com.moe.music.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
public class TokenService {

	@Value("${app.jwtSecret}")
	private String jwtSecret;

	@Value("${app.expiration}")
	private int jwtExpirationMs;

	private final UserJPA userJPA;

	@Autowired
	public TokenService(UserJPA userJPA) {
		this.userJPA = userJPA;
	}

	/**
	 * Tạo JWT Token cho người dùng.
	 *
	 * @param user Đối tượng người dùng
	 * @return Chuỗi JWT Token
	 */
	public String generateJwtToken(User user) {
		return Jwts.builder().setSubject(user.getEmail()).claim("userId", user.getUserId()).setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
				.signWith(SignatureAlgorithm.HS256, jwtSecret).compact();
	}

	/**
	 * Kiểm tra tính hợp lệ của token.
	 *
	 * @param token token cần kiểm tra
	 * @return true nếu token hợp lệ và chưa hết hạn, ngược lại false
	 */
	public boolean validateJwtToken(String token) {
		try {
			Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
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
		return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getSubject();
	}

	/**
	 * Lấy thời gian hết hạn từ JWT token.
	 *
	 * @param token JWT token
	 * @return Thời gian hết hạn của token
	 */
	public Date getExpirationDateFromJwtToken(String token) {
		Claims claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
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
		String refreshToken = UUID.randomUUID().toString();
		user.setRefreshToken(refreshToken);
		userJPA.save(user);
		return refreshToken;
	}

	/**
	 * Kiểm tra Refresh Token có hợp lệ với người dùng hay không.
	 *
	 * @param user         Đối tượng người dùng
	 * @param refreshToken Token cần kiểm tra
	 * @return true nếu refresh token hợp lệ, ngược lại false
	 */
	public boolean validateRefreshToken(User user, String refreshToken) {
		return refreshToken.equals(user.getRefreshToken());
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
