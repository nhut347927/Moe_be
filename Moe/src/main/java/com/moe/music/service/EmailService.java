package com.moe.music.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Gửi email đặt lại mật khẩu với giao diện HTML cho người dùng.
     *
     * @param email Địa chỉ email của người dùng
     * @param resetToken Token đặt lại mật khẩu
     */
    public void sendPasswordResetEmail(String email, String resetToken) {
        String subject = "Password Reset Request";
        String message = generateEmailContent(resetToken);

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(message, true); // Enable HTML content

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace(); // Handle error or log as needed
        }
    }

    /**
     * Tạo nội dung email với HTML, bao gồm token trung tâm và nút copy.
     *
     * @param resetToken Token đặt lại mật khẩu
     * @return Nội dung email dưới dạng chuỗi HTML
     */
    private String generateEmailContent(String resetToken) {
        return "<html>" +
                "<body style='font-family: Arial, sans-serif; text-align: center;'>" +
                "<h2>Password Reset Request</h2>" +
                "<p>You requested to reset your password. Use the token below:</p>" +
                "<div style='font-size: 20px; font-weight: bold; margin: 20px auto; padding: 10px; " +
                "background-color: #f0f0f0; border-radius: 8px; display: inline-block;'>" + resetToken + "</div>" +
                "<p>" +
                "<button style='padding: 10px 20px; font-size: 16px; color: white; background-color: #4CAF50; border: none; " +
                "border-radius: 5px; cursor: pointer;' onclick='navigator.clipboard.writeText(\"" + resetToken + "\")'>" +
                "Copy Token" +
                "</button>" +
                "</p>" +
                "<p>If you did not request a password reset, please ignore this email.</p>" +
                "</body>" +
                "</html>";
    }
}
