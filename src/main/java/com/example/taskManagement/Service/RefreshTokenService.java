package com.example.taskManagement.Service;

import com.example.taskManagement.Model.RefreshToken;
import com.example.taskManagement.Model.User;

import java.util.UUID;

public interface RefreshTokenService {
    RefreshToken findByUserId(UUID userId);
    RefreshToken createOrUpdate(User user);
    RefreshToken findByToken(String token);
    void verifyExpiration(RefreshToken refreshToken);
    void deleteToken(RefreshToken refreshToken);
}
