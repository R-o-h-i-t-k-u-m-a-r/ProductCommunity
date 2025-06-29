package com.productcommunity.service.email;

import com.productcommunity.exceptions.ResourceNotFoundException;
import com.productcommunity.model.User;
import com.productcommunity.repository.UserRepository;
import com.productcommunity.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final IUserService userService;

    @Value("${app.base-url}")
    private String baseUrl;

    public void initiatePasswordReset(String email) {
        if(!userRepository.existsByUserName(email) || !userRepository.findByUserName(email).isEnabled())
        {
            throw new ResourceNotFoundException("User not found or not verified");
        }
        User user = userRepository.findByUserName(email);

        String token = UUID.randomUUID().toString();
        user.setPasswordResetToken(token);
        user.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(2)); // 2 hour expiry
        userRepository.save(user);

        String resetUrl = "http://localhost:4200/reset-password?token=" + token;
        String emailContent = "<html><body style='font-family: Arial, sans-serif;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd;'>" +
                "<h2 style='color: #333;'>Password Reset Request</h2>" +
                "<p>Hello " + user.getUserName() + ",</p>" +
                "<p>We received a request to reset your password. Click the button below to proceed:</p>" +
                "<a href='" + resetUrl + "' style='" +
                "display: inline-block; padding: 10px 20px; " +
                "background-color: #4CAF50; color: white; " +
                "text-decoration: none; border-radius: 4px;'>" +
                "Reset Password</a>" +
                "<p>This link will expire in 2 hours.</p>" +
                "<p>If you didn't request this, please ignore this email.</p>" +
                "</div></body></html>";

        emailService.sendHtmlEmail(user.getUserName(), "Password Reset Request", emailContent);
    }

    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        userRepository.save(user);

        String emailContent = "<html><body style='font-family: Arial, sans-serif;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd;'>" +
                "<h2 style='color: #333;'>Password Reset Request</h2>" +
                "<p>Hello " + user.getUserName() + ",</p>" +
                "<p>You have successfully reset your password :</p>" +
                "<p>This link will expire in 2 hours.</p>" +
                "</div></body></html>";

        emailService.sendHtmlEmail(user.getUserName(), "Password Reset Successfully", emailContent);
    }
}
