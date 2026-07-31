package com.example.taskManagement.Service;

import com.example.taskManagement.Model.RefreshToken;
import com.example.taskManagement.Model.User;
import com.example.taskManagement.Repository.RefreshTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RefreshTokenServiceImpl.class);
    private static final long REFRESH_TOKEN_EXPIRY_DAYS = 7;
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenServiceImpl(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public RefreshToken findByUserId(UUID userId) {
        LOGGER.info("Fetching refresh token for userId={}", userId);
        return refreshTokenRepository.findByUser_Id(userId).orElse(null);
    }

    @Override
    public RefreshToken createOrUpdate(User user) {
        LOGGER.info("Creating/updating refresh token for user {}", user.getId());
        RefreshToken refreshToken = findByUserId(user.getId());
        if (refreshToken == null) {
            LOGGER.info("No existing refresh token found. Creating a new one for user {}", user.getId());
            refreshToken = new RefreshToken();
            refreshToken.setUser(user);
        } else {
            LOGGER.info("Existing refresh token found. Updating token for user {}", user.getId());
        }
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(REFRESH_TOKEN_EXPIRY_DAYS));
        refreshToken = refreshTokenRepository.save(refreshToken);
        LOGGER.info("Refresh token {} successfully created/updated for user {}", refreshToken.getId(), user.getId());
        return refreshToken;
    }

    @Override
    public RefreshToken findByToken(String token) {
        LOGGER.info("Fetching refresh token");
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token).orElse(null);
        if (refreshToken == null) {
            LOGGER.warn("Refresh token not found");
        }
        return refreshToken;
    }

    @Override
    public boolean verifyExpiration(RefreshToken refreshToken) {
        LOGGER.info("Checking refresh token expiry for user {}", refreshToken.getUser().getId());
        return refreshToken.getExpiryDate().isAfter(LocalDateTime.now());
    }

    @Override
    public void deleteToken(RefreshToken refreshToken) {
        LOGGER.info("Deleting refresh token for user {}", refreshToken.getUser().getId());
        refreshTokenRepository.deleteByUser(refreshToken.getUser());
        LOGGER.info("Refresh token deleted successfully for user {}", refreshToken.getUser().getId());
    }

    @Override
    public void deleteToken(User user) {
        LOGGER.info("Deleting refresh token for user {}", user.getId());
        refreshTokenRepository.deleteByUser(user);
        LOGGER.info("Refresh token deleted successfully for user {}", user.getId());
    }
}