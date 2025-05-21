package com.moe.socialnetwork.auth.services;

import java.time.LocalDateTime;

import com.moe.socialnetwork.common.models.User;
import com.moe.socialnetwork.exception.AppException;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;

public interface ITokenService {

    /**
     * Extract all claims from a JWT token.
     *
     * @param token JWT token
     * @return Claims extracted from token
     * @throws AppException if token is invalid or expired
     */
    Claims extractAllClaims(String token) throws AppException;

    /**
     * Generate a JWT access token for a user.
     *
     * @param user User object
     * @return JWT token string
     * @throws AppException if user or user email is invalid
     */
    String generateJwtToken(User user) throws AppException;

    /**
     * Generate an access token from a refresh token.
     *
     * @param refreshToken Refresh token
     * @return New access token
     * @throws AppException if refresh token is invalid or expired
     */
    String generateAccessTokenFromRefreshToken(String refreshToken) throws AppException;

    /**
     * Validate a JWT token.
     *
     * @param token JWT token
     * @return true if token is valid, false otherwise
     */
    boolean validateJwtToken(String token);

    /**
     * Get email from a JWT token.
     *
     * @param token JWT token
     * @return Email from token
     * @throws AppException if token is invalid or expired
     */
    String getEmailFromJwtToken(String token) throws AppException;

    /**
     * Get expiration date from a JWT token.
     *
     * @param token JWT token
     * @return Expiration date as LocalDateTime
     * @throws AppException if token is invalid or expired
     */
    LocalDateTime getExpirationDateFromJwtToken(String token) throws AppException;

    /**
     * Check if a token is expired.
     *
     * @param token JWT token
     * @return true if token is expired, false otherwise
     */
    boolean isTokenExpired(String token);

    /**
     * Generate a refresh token for a user.
     *
     * @param user User object
     * @return Refresh token string
     * @throws AppException if user or user email is invalid
     */
    String generateRefreshToken(User user) throws AppException;

    /**
     * Extract refresh token from HTTP request cookies.
     *
     * @param request HTTP request
     * @return Refresh token if found, null otherwise
     */
    String extractRefreshTokenFromCookie(HttpServletRequest request);

    /**
     * Extract access token from HTTP request cookies.
     *
     * @param request HTTP request
     * @return Access token if found, null otherwise
     */
    String extractAccessTokenFromCookie(HttpServletRequest request);

    /**
     * Generate a password reset token and save it to the user.
     *
     * @param user User object
     * @return Password reset token
     * @throws AppException if user is null
     */
    String generatePasswordResetToken(User user) throws AppException;

    /**
     * Validate a password reset token.
     *
     * @param user  User object
     * @param token Password reset token
     * @return true if token is valid and not expired, false otherwise
     */
    boolean validatePasswordResetToken(User user, String token);

    /**
     * Clear password reset token for a user.
     *
     * @param user User object
     * @throws AppException if user is null
     */
    void clearTokens(User user) throws AppException;

}
