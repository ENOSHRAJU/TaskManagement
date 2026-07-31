package com.example.taskManagement.Service;

import com.example.taskManagement.Model.EmailVerificationToken;
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
    public void sendVerificationEmail(User user, EmailVerificationToken verificationToken) {
        String verificationLink = "http://localhost:8080/auth/verify-email?token="
                + verificationToken.getToken();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(EMAIL_SENDER);
        message.setTo(user.getEmail());
        message.setSubject("Verify your Task Management account");
        message.setText("""
            Hello %s,

            Welcome to Task Management!

            Thank you for registering your account.

            To activate your account and start using the application, please verify your email address by clicking the link below:

            %s

            This verification link is valid for 24 hours.

            If you did not create this account, you can safely ignore this email.

            Regards,
            Task Management Team
            """.formatted(user.getName(), verificationLink));

        try {
            mailSender.send(message);
            LOGGER.info("Verification email sent successfully to {}", user.getEmail());
        } catch (Exception ex) {
            LOGGER.error("Failed to send verification email to {}",
                    user.getEmail(), ex);
        }
    }

    @Override
    @Async
    public void sendPasswordResetEmail(User user, PasswordResetToken resetToken) {
        String resetLink = "http://localhost:8080/auth/reset-password?token="
                + resetToken.getToken();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(EMAIL_SENDER);
        message.setTo(user.getEmail());
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
