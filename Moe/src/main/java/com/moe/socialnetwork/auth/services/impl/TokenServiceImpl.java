package com.moe.socialnetwork.auth.services.impl;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.moe.socialnetwork.auth.services.ITokenService;
import com.moe.socialnetwork.common.models.User;
import com.moe.socialnetwork.common.repositories.UserRepository;
import com.moe.socialnetwork.exception.AppException;

import com.moe.socialnetwork.util.AuthorityUtil;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Key;

@Service
public class TokenServiceImpl implements ITokenService {

    private final Key key;
    private final UserRepository userJPA;

    @Value("${app.expiration24h}")
    private Long jwtExpirationMs24h;

    @Value("${app.expiration6months}")
    private Long jwtExpirationMs6months;

    @Value("${app.jwtSecret}")
    private String jwtSecret;

    public TokenServiceImpl(UserRepository userJPA, @Value("${app.jwtSecret}") String jwtSecret) {
        this.userJPA = userJPA;
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalArgumentException("JWT secret key must not be null or empty.");
        }
        byte[] secretBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalArgumentException("JWT secret key must be at least 32 bytes long.");
        }
        this.key = Keys.hmacShaKeyFor(secretBytes);
    }

    /**
     * Parse JWT token and extract claims.
     *
     * @param token JWT token
     * @return Claims extracted from token
     * @throws AppException if token is invalid or expired
     */
    private Claims parseClaims(String token) {
        if (token == null || token.isBlank()) {
            throw new AppException("Token must not be null or empty", HttpStatus.BAD_REQUEST.value());
        }
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            throw new AppException("Expired JWT token", HttpStatus.UNAUTHORIZED.value());
        } catch (JwtException e) {
            throw new AppException("Invalid JWT token: " + e.getMessage(), HttpStatus.UNAUTHORIZED.value());
        }
    }

    /**
     * Extract all claims from a JWT token.
     *
     * @param token JWT token
     * @return Claims
     */
    public Claims extractAllClaims(String token) {
        return parseClaims(token);
    }

    /**
     * Generate a JWT access token for a user.
     *
     * @param user User object
     * @return JWT token string
     */
    public String generateJwtToken(User user) {
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
            throw new AppException("User or user email must not be null or empty", HttpStatus.BAD_REQUEST.value());
        }
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("roles", AuthorityUtil.convertToAuthorities(user.getRolePermissions()))
                .claim("uuid", UUID.randomUUID().toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs24h))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Generate an access token from a refresh token.
     *
     * @param refreshToken Refresh token
     * @return New access token
     */
    public String generateAccessTokenFromRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AppException("Refresh token must not be null or empty", HttpStatus.BAD_REQUEST.value());
        }
        if (isTokenExpired(refreshToken)) {
            throw new AppException("Refresh token is expired", HttpStatus.UNAUTHORIZED.value());
        }
        String email = parseClaims(refreshToken).getSubject();
        User user = userJPA.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found for email: " + email));
        return generateJwtToken(user);
    }

    /**
     * Validate a JWT token.
     *
     * @param token JWT token
     * @return true if token is valid, false otherwise
     */
    public boolean validateJwtToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * Get email from a JWT token.
     *
     * @param token JWT token
     * @return Email from token
     */
    public String getEmailFromJwtToken(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Get expiration date from a JWT token.
     *
     * @param token JWT token
     * @return Expiration date as LocalDateTime
     */
    public LocalDateTime getExpirationDateFromJwtToken(String token) {
        Claims claims = parseClaims(token);
        return claims.getExpiration().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Check if a token is expired.
     *
     * @param token JWT token
     * @return true if token is expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        if (token == null || token.isBlank()) {
            return true;
        }
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return false;
        } catch (JwtException e) {
            return true;
        }
    }

    /**
     * Generate a refresh token for a user.
     *
     * @param user User object
     * @return Refresh token string
     */
    public String generateRefreshToken(User user) {
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
            throw new AppException("User or user email must not be null or empty", HttpStatus.BAD_REQUEST.value());
        }
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("uuid", UUID.randomUUID().toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs6months))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extract refresh token from HTTP request cookies.
     *
     * @param request HTTP request
     * @return Refresh token if found, null otherwise
     */
    public String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if ("refresh_token".equals(cookie.getName()) && !cookie.getValue().isBlank()) {
                return cookie.getValue();
            }
        }
        return null;
    }

    /**
     * Extract access token from HTTP request cookies.
     *
     * @param request HTTP request
     * @return Access token if found, null otherwise
     */
    public String extractAccessTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if ("access_token".equals(cookie.getName()) && !cookie.getValue().isBlank()) {
                return cookie.getValue();
            }
        }
        return null;
    }

    /**
     * Generate a password reset token and save it to the user.
     *
     * @param user User object
     * @return Password reset token
     */
    public String generatePasswordResetToken(User user) {
        if (user == null) {
            throw new AppException("User cannot be null", HttpStatus.BAD_REQUEST.value());
        }
        String resetToken = UUID.randomUUID().toString();
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetExpires(LocalDateTime.now().plusHours(1));
        userJPA.save(user);
        return resetToken;
    }

    /**
     * Validate a password reset token.
     *
     * @param user User object
     * @param token Password reset token
     * @return true if token is valid and not expired, false otherwise
     */
    public boolean validatePasswordResetToken(User user, String token) {
        if (user == null || token == null || token.isBlank()) {
            return false;
        }
        return token.equals(user.getPasswordResetToken())
                && user.getPasswordResetExpires() != null
                && user.getPasswordResetExpires().isAfter(LocalDateTime.now());
    }

    /**
         * Clear password reset token for a user.
     *
     * @param user User object
     */
    public void clearTokens(User user) {
        if (user == null) {
            throw new AppException("User cannot be null", HttpStatus.BAD_REQUEST.value());
        }
        user.setPasswordResetToken(null);
        user.setPasswordResetExpires(null);
        userJPA.save(user);
    }
}