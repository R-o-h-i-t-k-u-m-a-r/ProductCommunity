package com.productcommunity.service.email;

import com.productcommunity.exceptions.ResourceNotFoundException;
import com.productcommunity.model.User;
import com.productcommunity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Value("${app.base-url}")
    private String baseUrl;

    public void sendVerificationEmail(User user) {
        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        user.setVerificationTokenExpiry(LocalDateTime.now().plusHours(24));
        userRepository.save(user);

        String emailContent = getEmailContent(token);

        emailService.sendHtmlEmail(user.getUserName(), "Verify your email", emailContent);
        //emailService.sendEmail(user.getUserName(),"ShoppingCart Email Verification",verificationUrl);
    }

    private String getEmailContent(String token) {
        String verificationUrl = baseUrl + "/api/v1/public/verify-email?token=" + token;
        return "<html><body>" +
                "<h2>Welcome to ShoppingCart!</h2>" +
                "<p>Please click the button below to verify your email address:</p>" +
                "<a href=\"" + verificationUrl + "\" style=\"background-color: #4CAF50; color: white; padding: 10px 20px; text-decoration: none;\">Verify Email</a>" +
                "<p>This link will expire in 24 hours.</p>" +
                "</body></html>";
    }

    public void verifyUser(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new ResourceNotFoundException("Token expired");
        }

        user.setEnabled(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        userRepository.save(user);

    }
}
