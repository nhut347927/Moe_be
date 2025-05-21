package com.moe.socialnetwork.api.services;

public interface IEmailService {

    /**
     * Send a password reset email with HTML content to the user.
     *
     * @param email      User's email address
     * @param resetToken Password reset token
     * @throws AppException if email sending fails
     */
    void sendPasswordResetEmail(String email, String resetToken);
}