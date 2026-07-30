package com.example.taskManagement.Service;

import com.example.taskManagement.Model.RefreshToken;
import com.example.taskManagement.Model.User;

import java.util.UUID;

public class RefreshTokenServiceImpl implements RefreshTokenService{

    @Override
    public RefreshToken findByUserId(UUID userId) {
        return null;
    }

    @Override
    public RefreshToken createOrUpdate(User user) {
        return null;
    }

    @Override
    public RefreshToken findByToken(String token) {
        return null;
    }

    @Override
    public void verifyExpiration(RefreshToken refreshToken) {

    }

    @Override
    public void deleteToken(RefreshToken refreshToken) {

    }
}
