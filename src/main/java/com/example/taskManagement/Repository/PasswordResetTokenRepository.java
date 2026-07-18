package com.example.taskManagement.Repository;

import com.example.taskManagement.Model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    Optional<PasswordResetToken> findByUser_Id(UUID userId);
    Optional<PasswordResetToken> findByToken(String token);
}
