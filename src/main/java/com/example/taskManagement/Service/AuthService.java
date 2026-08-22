package com.example.taskManagement.Service;

import com.example.taskManagement.Configurations.CustomUserDetails;
import com.example.taskManagement.Configurations.CustomUserDetailsService;
import com.example.taskManagement.Configurations.JwtService;
import com.example.taskManagement.Configurations.SecurityUtil;
import com.example.taskManagement.DTOs.*;
import com.example.taskManagement.Enums.AuditAction;
import com.example.taskManagement.Enums.AuditEntityType;
import com.example.taskManagement.Exception.*;
import com.example.taskManagement.Enums.RoleTypes;
import com.example.taskManagement.Model.EmailVerificationToken;
import com.example.taskManagement.Model.PasswordResetToken;
import com.example.taskManagement.Model.RefreshToken;
import com.example.taskManagement.Model.User;
import com.example.taskManagement.Repository.PasswordResetTokenRepository;
import com.example.taskManagement.Repository.RoleRepository;
import com.example.taskManagement.Repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final AuditService auditService;
    private final PasswordResetTokenService passwordResetTokenService;
    private final EmailService emailService;
    private final EmailVerificationTokenService emailVerificationTokenService;
    private final RefreshTokenService refreshTokenService;
    private final CustomUserDetailsService customUserDetailsService;
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository, AuthenticationManager authenticationManager, JwtService jwtService, UserService userService, AuditService auditService, SecurityUtil securityUtil, PasswordResetTokenRepository resetTokenRepository, PasswordResetTokenService passwordResetTokenService, PasswordResetTokenRepository passwordResetTokenRepository, EmailService emailService, EmailVerificationTokenService emailVerificationTokenService, RefreshTokenService refreshTokenService, CustomUserDetailsService customUserDetailsService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
        this.auditService = auditService;
        this.passwordResetTokenService = passwordResetTokenService;
        this.emailService = emailService;
        this.emailVerificationTokenService = emailVerificationTokenService;
        this.refreshTokenService = refreshTokenService;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Transactional
    public String registerUser(RegisterDTO registerDTO) {
        LOGGER.info("Registration request received for email: {}", registerDTO.getEmail());
        User user = userService.getUserByEmail(registerDTO.getEmail());
        if(user != null) {
            if(user.isActive()) {
                LOGGER.warn("Registration failed: Email already exists: {}", registerDTO.getEmail());
                throw new DuplicateEmailException("Email already exists");
            }
            LOGGER.info("Unverified user re-registering, updating details for userId: {}", user.getId());
            user.setName(registerDTO.getName());
            user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
            userService.saveUser(user);
            auditService.log(AuditEntityType.USER, user.getId(), AuditAction.UPDATE, "Name", " - ", user.getName(), user.getId());
            auditService.log(AuditEntityType.USER, user.getId(), AuditAction.UPDATE, "Password", "[REDACTED]", "[REDACTED]", user.getId());        } else {
            user = new User();
            user.setName(registerDTO.getName());
            user.setEmail(registerDTO.getEmail());
            user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
            user.getRoles().add(roleRepository.findByRole(RoleTypes.MANAGER.toString())
                    .orElseThrow(() -> new IllegalStateException("Role not found")));
            userService.saveUser(user);
            LOGGER.info("User registered successfully: {}, with email: {}", user.getId(), user.getEmail());
            auditService.log(AuditEntityType.USER,user.getId(), AuditAction.CREATE,"User", " - ", user.getEmail(), user.getId());
        }
        EmailVerificationToken verificationToken = emailVerificationTokenService.createOrUpdate(user);
        emailService.sendVerificationEmail(user, verificationToken);
        LOGGER.info("Verification email sent to user {}", user.getId());
        return "Registration request processed successfully. Please check your email to verify your account.";
    }

    @Transactional
    public String verifyEmail(String token) {
        EmailVerificationToken emailVerificationToken = emailVerificationTokenService.findByToken(token);
        if(emailVerificationToken == null) {
            LOGGER.warn("Invalid email verification token");
            throw new InvalidEmailVerificationToken("Invalid email verification token");
        }
        User user = emailVerificationToken.getUser();
        if(user.isActive()) {
            LOGGER.info("Email is already verified for user {}", user.getId());
            return "Email is already verified";
        }
        if (emailVerificationTokenService.checkTokenExpiry(emailVerificationToken)) {
            LOGGER.warn("Expired verification token for user {}", user.getId());
            emailVerificationTokenService.deleteToken(emailVerificationToken);
            EmailVerificationToken newToken = emailVerificationTokenService.createOrUpdate(user);
            emailService.sendVerificationEmail(user, newToken);
            LOGGER.info("New verification email sent to user {}", user.getId());
            throw new InvalidEmailVerificationToken(
                    "Your verification link has expired. A new link has been sent to your email.");
        }
        user.setActive(true);
        userRepository.save(user);
        emailVerificationTokenService.deleteToken(emailVerificationToken);
        auditService.log(AuditEntityType.USER, user.getId(), AuditAction.ACTIVATE_ACCOUNT, "Active", "False", "True", user.getId());
        LOGGER.info("User {} successfully verified their email.", user.getId());
        return "Email verified successfully";
    }

    public LoginResponseDTO login(LoginRequestDTO requestDTO) {
        LOGGER.info("Login request received for email: {}", requestDTO.getEmail());
        Authentication auth = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(
                        requestDTO.getEmail(),
                        requestDTO.getPassword())
                );
        LOGGER.info("Authentication successful for email: {}", requestDTO.getEmail());
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        if(!userDetails.isEnabled()) {
            LOGGER.warn("Login attempt for inactive account: {}", requestDTO.getEmail());
            throw new AccountNotVerifiedException("Your account has not been verified. Please verify your email before logging in.");
        }
        User user = userService.findUserById(userDetails.getId());
        String accessToken = jwtService.generateToken(userDetails);
        LOGGER.info("Access token generated for user {}", user.getId());
        RefreshToken refreshToken = refreshTokenService.createOrUpdate(user);
        LOGGER.info("Refresh token generated for user {}", user.getId());
        LoginResponseDTO responseDTO = LoginResponseDTO.builder()
                        .userId(userDetails.getId())
                        .email(userDetails.getUsername())
                        .primaryRole(userService.getHighestAuthRole(userService.getUserRoles(userDetails)))
                        .accessToken(accessToken)
                        .refreshToken(refreshToken.getToken())
                        .build();
        LOGGER.info("Login successful for user: {}", userDetails.getUsername());
        return responseDTO;
    }

    @Transactional
    public RefreshTokenResponseDTO VerifyRefreshToken(String token) {
        LOGGER.info("Refresh token request received");
        RefreshToken refreshToken = refreshTokenService.findByToken(token);
        if (refreshToken == null) {
            LOGGER.warn("Invalid refresh token");
            throw new InvalidRefreshTokenException("Invalid refresh token");
        }
        if (!refreshTokenService.verifyExpiration(refreshToken)) {
            LOGGER.warn("Expired refresh token for user {}", refreshToken.getUser().getId());
            refreshTokenService.deleteToken(refreshToken);
            throw new InvalidRefreshTokenException("Refresh token expired. Please login again.");
        }
        User user = refreshToken.getUser();
        CustomUserDetails userDetails = (CustomUserDetails) customUserDetailsService
                .loadUserByUsername(user.getEmail());
        String accessToken = jwtService.generateToken(userDetails);
        LOGGER.info("New access token generated for user {}", user.getId());
        return RefreshTokenResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())   // reuse same refresh token
                .build();
    }

    public String handleForgotPassword(String email) {
        LOGGER.info("Forgot password request received for email: {}", email);
        if(!userService.existsUserByEmail(email)) {
            LOGGER.warn("Forgot password requested for non-existing email: {}", email);
            throw new UsernameNotFoundException("User not found with email: "+ email);
        }
        LOGGER.info("Finding user with email: {}", email);
        User user = userService.findUserByEmail(email);
        LOGGER.info("Found user to reset password: {}", email);
        PasswordResetToken resetToken = passwordResetTokenService.createOrUpdate(user);
        emailService.sendPasswordResetEmail(user, resetToken);
        return "Password reset process initiated";
    }

    @Transactional
    public String handleResetPassword(String token, PasswordResetDTO resetDTO) {
        LOGGER.info("Password reset request received");
        PasswordResetToken resetToken = passwordResetTokenService.findUserFromToken(token);
        User user = resetToken.getUser();
        passwordResetTokenService.checkTokenExpiry(resetToken);
        LOGGER.debug("User successfully loaded: {}", user.getName());
        if (passwordEncoder.matches(resetDTO.getNewPassword(), user.getPassword())) {
            LOGGER.warn("User attempted to reuse the existing password. userId={}", user.getId());
            throw new SamePasswordException("New password must be different from old password.");
        }
        user.setPassword(passwordEncoder.encode(resetDTO.getNewPassword()));
        LOGGER.info("Password reset successful for userId: {}", user.getId());
        passwordResetTokenService.deleteToken(resetToken);
        auditService.log(AuditEntityType.USER, user.getId(), AuditAction.UPDATE, "Password", "[REDACTED]", "[REDACTED]", user.getId());        return "Password reset successfully";
    }

    @Transactional
    public String logout(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        LOGGER.info("Logout request received for user {}", userDetails.getId());
        User user = userService.findUserById(userDetails.getId());
        refreshTokenService.deleteToken(user);
        LOGGER.info("Refresh token deleted for user {}", user.getId());
        LOGGER.info("User {} logged out successfully", user.getId());
        return "User logged out successfully";
    }
}
