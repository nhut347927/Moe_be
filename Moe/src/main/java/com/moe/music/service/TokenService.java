package com.moe.music.service;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class TokenService {

	// Lấy key bí mật và thời gian hết hạn từ file cấu hình
	private final SecretKey secretKey;
	private final long expirationTimeMs;

	// Khởi tạo secretKey từ file application.properties
	public TokenService(@Value("${jwt.secret}") String secretKey, @Value("${jwt.expiration}") long expirationTimeMs) {
		this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes());
		this.expirationTimeMs = expirationTimeMs;
	}

	/**
	 * Tạo token từ username.
	 *
	 * @param username tên người dùng cần mã hóa vào token
	 * @return JWT token đã mã hóa
	 */
	public String generateToken(String username) {
		return Jwts.builder().setSubject(username).setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + expirationTimeMs))
				.signWith(secretKey, SignatureAlgorithm.HS256).compact();
	}

	/**
	 * Kiểm tra tính hợp lệ của token.
	 *
	 * @param token token cần kiểm tra
	 * @return true nếu token hợp lệ, ngược lại false
	 */
	public boolean validateToken(String token) {
		try {
			Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Trích xuất username từ token.
	 *
	 * @param token JWT token
	 * @return username trích xuất từ token
	 */
	public String getUsernameFromToken(String token) {
		Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
		return claims.getSubject();
	}

	/**
	 * Trích xuất ngày hết hạn từ token.
	 *
	 * @param token JWT token
	 * @return Ngày hết hạn của token
	 */
	public Date getExpirationDateFromToken(String token) {
		Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
		return claims.getExpiration();
	}

	/**
	 * Kiểm tra token đã hết hạn chưa.
	 *
	 * @param token JWT token
	 * @return true nếu token đã hết hạn, ngược lại false
	 */
	public boolean isTokenExpired(String token) {
		Date expiration = getExpirationDateFromToken(token);
		return expiration.before(new Date());
	}
}
