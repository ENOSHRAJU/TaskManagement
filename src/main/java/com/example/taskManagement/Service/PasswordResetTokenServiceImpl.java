package com.example.taskManagement.Service;

import com.example.taskManagement.DTOs.PasswordResetDTO;
import com.example.taskManagement.Exception.ResetTokenExpired;
import com.example.taskManagement.Exception.ResetTokenNotFound;
import com.example.taskManagement.Model.PasswordResetToken;
import com.example.taskManagement.Model.User;
import com.example.taskManagement.Repository.PasswordResetTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetTokenServiceImpl implements PasswordResetTokenService {

    private static final long RESET_TOKEN_EXPIRY_MINUTES = 15;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordResetTokenServiceImpl.class);

    public PasswordResetTokenServiceImpl(PasswordResetTokenRepository passwordResetTokenRepository) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    @Override
    public PasswordResetToken findByUserId(UUID userId) {
        LOGGER.info("Fetching password reset token for userId={}", userId);
        return passwordResetTokenRepository.findByUser_Id(userId).orElse(null);
    }

    @Override
    public PasswordResetToken createOrUpdate(User user) {
        PasswordResetToken resetToken = findByUserId(user.getId());
        if (resetToken == null) {
            resetToken = new PasswordResetToken();
            resetToken.setUser(user);
        }
        resetToken.setToken(UUID.randomUUID().toString());
        resetToken.setExpiryTime(LocalDateTime.now().plusMinutes(RESET_TOKEN_EXPIRY_MINUTES));
        LOGGER.info("Creating/Updating password reset token for userId {}", user.getId());
        passwordResetTokenRepository.save(resetToken);
        return resetToken;
    }

    @Override
    public PasswordResetToken findUserFromToken(String token) {
        return passwordResetTokenRepository.findByToken(token).orElseThrow(() -> {
                LOGGER.warn("Reset token not found: {}", token);
                return new ResetTokenNotFound("Reset token not found "+ token);
        });
    }

    @Override
    public void checkTokenExpiry(PasswordResetToken resetToken) {
        if(resetToken.getExpiryTime().isBefore(LocalDateTime.now())){
            LOGGER.warn("Reset token is expired");
            throw new ResetTokenExpired("Reset token expired");
        }
    }

    @Override
    public void deleteToken(PasswordResetToken resetToken) {
        passwordResetTokenRepository.delete(resetToken);
        LOGGER.info("Password reset token deleted for user: {}", resetToken.getUser().getId());
    }
}
