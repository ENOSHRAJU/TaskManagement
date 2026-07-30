package com.example.taskManagement.Repository;

import com.example.taskManagement.Model.EmailVerificationToken;
import com.example.taskManagement.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {
    Optional<EmailVerificationToken> findByToken(UUID token);
    Optional<EmailVerificationToken> findByUser_Id(UUID userId);
}
