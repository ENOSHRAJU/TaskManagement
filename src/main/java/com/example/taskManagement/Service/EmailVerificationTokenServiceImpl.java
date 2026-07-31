package com.example.taskManagement.Service;

import com.example.taskManagement.Exception.InvalidEmailVerificationToken;
import com.example.taskManagement.Model.EmailVerificationToken;
import com.example.taskManagement.Model.User;
import com.example.taskManagement.Repository.EmailVerificationTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class EmailVerificationTokenServiceImpl implements EmailVerificationTokenService{

    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private static final long EMAIL_VERIFICATION_TOKEN_EXPIRY_HOURS = 24;
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailVerificationTokenServiceImpl.class);

    public EmailVerificationTokenServiceImpl(EmailVerificationTokenRepository emailVerificationTokenRepository) {
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
    }

    @Override
    public EmailVerificationToken findByUserId(UUID userId) {
        LOGGER.info("Fetching email verification token for userId={}", userId);
        return emailVerificationTokenRepository.findByUser_Id(userId).orElse(null);
    }

    @Override
    public EmailVerificationToken createOrUpdate(User user) {
        EmailVerificationToken verificationToken = findByUserId(user.getId());
        if(verificationToken == null) {
            verificationToken = new EmailVerificationToken();
            verificationToken.setUser(user);
        }
        verificationToken.setToken(UUID.randomUUID().toString());
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(EMAIL_VERIFICATION_TOKEN_EXPIRY_HOURS));
        emailVerificationTokenRepository.save(verificationToken);
        LOGGER.info("Email verification token {} has been successfully created for user {}", verificationToken.getId(), user.getId());
        return verificationToken;
    }

    @Override
    public EmailVerificationToken findByToken(String token) {
        return emailVerificationTokenRepository.findByToken(token).orElse(null);
    }

    @Override
    public void checkTokenExpiry(EmailVerificationToken verificationToken) {
        // Temporarily not including expiry check
    }

    @Override
    public void deleteToken(EmailVerificationToken verificationToken) {
        emailVerificationTokenRepository.delete(verificationToken);
    }
}
