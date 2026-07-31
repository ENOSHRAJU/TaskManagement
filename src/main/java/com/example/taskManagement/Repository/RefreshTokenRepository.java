package com.example.taskManagement.Repository;

import com.example.taskManagement.Model.RefreshToken;
import com.example.taskManagement.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUser_Id(UUID userId);
    void deleteByUser(User user);
}
