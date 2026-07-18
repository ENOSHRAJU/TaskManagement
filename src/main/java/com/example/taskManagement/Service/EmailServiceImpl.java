package com.example.taskManagement.Service;

import com.example.taskManagement.Model.PasswordResetToken;
import com.example.taskManagement.Model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private static final String EMAIL_SENDER = "enoshraj76@gmail.com";
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailServiceImpl.class);

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    @Async
    public void sendPasswordResetEmail(User user, PasswordResetToken resetToken) {
        String resetLink = "http://localhost:8080/auth/reset-password?token="
                + resetToken.getToken();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(EMAIL_SENDER);
        //message.setTo(user.getEmail());
        message.setTo("rajenosh313@gmail.com");
        message.setSubject("Password Reset Request");
        message.setText("""
                Hello %s,

                We received a request to reset your password for your Task Management account.

                Please click the link below to reset your password:

                %s

                This link is valid for 15 minutes.

                If you did not request a password reset, you can safely ignore this email.
                Your password will remain unchanged.

                Regards,
                Task Management Team
                """.formatted(user.getName(), resetLink));
        try {
            mailSender.send(message);
            LOGGER.info("Password reset email sent successfully to {}", user.getEmail());
        }
        catch (Exception ex) {
            LOGGER.error("Failed to send password reset email to {}",
                    user.getEmail(), ex);
        }
    }
}
