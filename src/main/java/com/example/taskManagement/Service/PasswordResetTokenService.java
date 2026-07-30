package com.example.taskManagement.Service;

import com.example.taskManagement.Model.PasswordResetToken;
import com.example.taskManagement.Model.User;

import java.time.LocalDateTime;
import java.util.UUID;


public interface PasswordResetTokenService {
    PasswordResetToken findByUserId(UUID userId);
    PasswordResetToken createOrUpdate(User user);
    PasswordResetToken findUserFromToken(String token);
    void checkTokenExpiry(PasswordResetToken resetToken);
    void deleteToken(PasswordResetToken resetToken);
}
