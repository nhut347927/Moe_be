package com.moe.socialnetwork.api.services.impl;

import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.moe.socialnetwork.api.services.IEmailService;
import com.moe.socialnetwork.exception.AppException;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements IEmailService {

	private final JavaMailSender mailSender;

	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

	@Value("${app.email.from}")
	private String fromEmail;

	@Value("${app.email.reset-password-url}")
	private String resetPasswordUrl;

	@Value("${app.name}")
	private String appName;

	public EmailServiceImpl(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	/**
	 * Send a password reset email with HTML content to the user.
	 *
	 * @param email      User's email address
	 * @param resetToken Password reset token
	 * @throws AppException if email sending fails
	 */
	public void sendPasswordResetEmail(String email, String resetToken) {
		validateInputs(email, resetToken);
		String subject = appName + " - Password Reset Request";
		String message = generateEmailContent(resetToken);

		try {
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

			helper.setFrom(fromEmail);
			helper.setTo(email);
			helper.setSubject(subject);
			helper.setText(message, true); // Enable HTML content

			mailSender.send(mimeMessage);
		} catch (MessagingException e) {
			throw new AppException("Failed to send password reset email: " + e.getMessage(), 500);
		}
	}

	/**
	 * Validate email and reset token.
	 *
	 * @param email      Email address
	 * @param resetToken Reset token
	 * @throws AppException if validation fails
	 */
	private void validateInputs(String email, String resetToken) {
		if (email == null || email.isBlank()) {
			throw new AppException("Email must not be null or empty", 400);
		}
		if (!EMAIL_PATTERN.matcher(email).matches()) {
			throw new AppException("Invalid email format", 400);
		}
		if (resetToken == null || resetToken.isBlank()) {
			throw new AppException("Reset token must not be null or empty", 400);
		}
	}

	/**
	 * Generate HTML email content with a reset link.
	 *
	 * @param resetToken Password reset token
	 * @return HTML email content
	 */
	private String generateEmailContent(String resetToken) {
		String resetUrl = resetPasswordUrl + "?token=" + resetToken;
		return """
				<!DOCTYPE html>
				<html>
				<head>
				    <meta charset="UTF-8">
				    <meta name="viewport" content="width=device-width, initial-scale=1.0">
				    <style>
				        body { font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4; }
				        .container { max-width: 600px; margin: 20px auto; background-color: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }
				        h2 { color: #333333; text-align: center; }
				        p { color: #555555; line-height: 1.6; text-align: center; }
				        .button { display: inline-block; padding: 12px 24px; font-size: 16px; color: #ffffff; background-color: #4CAF50; text-decoration: none; border-radius: 5px; margin: 20px 0; }
				        .footer { font-size: 12px; color: #777777; text-align: center; margin-top: 20px; }
				        @media only screen and (max-width: 600px) {
				            .container { padding: 15px; }
				            .button { padding: 10px 20px; font-size: 14px; }
				        }
				    </style>
				</head>
				<body>
				    <div class="container">
				        <h2>Password Reset Request</h2>
				        <p>You have requested to reset your password for %s.</p>
				        <p>Click the button below to reset your password. This link is valid for 1 hour:</p>
				        <a href="%s" class="button">Reset Password</a>
				        <p>If you did not request a password reset, please ignore this email or contact our support team.</p>
				        <div class="footer">
				            <p>© %s. All rights reserved.</p>
				            <p>Contact us at <a href="mailto:support@%s.com">support@%s.com</a></p>
				        </div>
				    </div>
				</body>
				</html>
				"""
				.formatted(appName, resetUrl, appName, appName.toLowerCase(), appName.toLowerCase());
	}
}