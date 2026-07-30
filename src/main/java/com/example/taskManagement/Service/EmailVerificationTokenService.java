package com.example.taskManagement.Service;

import com.example.taskManagement.Model.EmailVerificationToken;
import com.example.taskManagement.Model.PasswordResetToken;
import com.example.taskManagement.Model.User;

import java.time.LocalDateTime;
import java.util.UUID;

public interface EmailVerificationTokenService {
    EmailVerificationToken findByUserId(UUID userId);
    EmailVerificationToken createOrUpdate(User user);
    EmailVerificationToken findByToken(String token);
    void checkTokenExpiry(EmailVerificationToken verificationToken);
    void deleteToken(EmailVerificationToken verificationToken);
}
