package com.example.taskManagement.Service;

import com.example.taskManagement.Model.EmailVerificationToken;
import com.example.taskManagement.Model.User;

import java.util.UUID;

public interface EmailVerificationTokenService {
    EmailVerificationToken findByUserId(UUID userId);
    EmailVerificationToken createOrUpdate(User user);
    EmailVerificationToken findByToken(String token);
    boolean checkTokenExpiry(EmailVerificationToken verificationToken);
    void deleteToken(EmailVerificationToken verificationToken);
}
