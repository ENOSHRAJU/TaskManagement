package com.example.taskManagement.Service;

import com.example.taskManagement.Configurations.CustomUserDetails;
import com.example.taskManagement.Configurations.JwtService;
import com.example.taskManagement.Configurations.SecurityUtil;
import com.example.taskManagement.DTOs.LoginRequestDTO;
import com.example.taskManagement.DTOs.LoginResponseDTO;
import com.example.taskManagement.DTOs.PasswordResetDTO;
import com.example.taskManagement.DTOs.RegisterDTO;
import com.example.taskManagement.Enums.AuditAction;
import com.example.taskManagement.Enums.AuditEntityType;
import com.example.taskManagement.Exception.DuplicateEmailException;
import com.example.taskManagement.Enums.RoleTypes;
import com.example.taskManagement.Exception.SamePasswordException;
import com.example.taskManagement.Model.EmailVerificationToken;
import com.example.taskManagement.Model.PasswordResetToken;
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
    private final EmailVerificationTokenService emailVerification;
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository, AuthenticationManager authenticationManager, JwtService jwtService, UserService userService, AuditService auditService, SecurityUtil securityUtil, PasswordResetTokenRepository resetTokenRepository, PasswordResetTokenService passwordResetTokenService, PasswordResetTokenRepository passwordResetTokenRepository, EmailService emailService, EmailVerificationTokenService emailVerification) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
        this.auditService = auditService;
        this.passwordResetTokenService = passwordResetTokenService;
        this.emailService = emailService;
        this.emailVerification = emailVerification;
    }

    @Transactional
    public String registerUser(RegisterDTO registerDTO) {
        LOGGER.info("Registration request received for email: {}", registerDTO.getEmail());
        if(userService.existsUserByEmail(registerDTO.getEmail())) {
            LOGGER.warn("Registration failed: Email already exists: {}", registerDTO.getEmail());
            throw new DuplicateEmailException("Email already exists");
        }
        User user = new User();
        user.setName(registerDTO.getName());
        user.setEmail(registerDTO.getEmail());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.getRoles().add(roleRepository.findByRole(RoleTypes.MANAGER.toString())
                .orElseThrow(() -> new IllegalStateException("Role not found")));
        userRepository.save(user);
        EmailVerificationToken verificationToken = emailVerification.createOrUpdate(user);
        LOGGER.info("Verification email sent to user {}", user.getId());
        LOGGER.info("User registered successfully userId: {}, email: {}", user.getId(), user.getEmail());
        auditService.log(AuditEntityType.USER,user.getId(), AuditAction.CREATE,"User", " - ", user.getEmail(), user.getId());
        emailService.sendVerificationEmail(user, verificationToken);
        return user.getEmail();
    }

    @Transactional
    public String registerUserTemp(RegisterDTO registerDTO) {
        LOGGER.info("Registration request received for email: {}", registerDTO.getEmail());
        User user = userService.findUserByEmail(registerDTO.getEmail());
        if(user == null) {
            user = new User();
            user.setName(registerDTO.getName());
            user.setEmail(registerDTO.getEmail());
            user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
            user.getRoles().add(roleRepository.findByRole(RoleTypes.MANAGER.toString())
                    .orElseThrow(() -> new IllegalStateException("Role not found")));
            userRepository.save(user);
            LOGGER.info("User registered successfully userId: {}, email: {}", user.getId(), user.getEmail());
            EmailVerificationToken verificationToken = emailVerification.createOrUpdate(user);
            LOGGER.info("Verification email sent to user {}", user.getId());
            auditService.log(AuditEntityType.USER,user.getId(), AuditAction.CREATE,"User", " - ", user.getEmail(), user.getId());
            emailService.sendVerificationEmail(user, verificationToken);
        } else if(user.isActive()) {
            LOGGER.warn("Registration failed: Email already exists: {}", registerDTO.getEmail());
            throw new DuplicateEmailException("Email already exists");
        } else {
            LOGGER.info("User: {} is registered before but not activated the account", user.getId());
            EmailVerificationToken verificationToken = emailVerification.createOrUpdate(user);
            LOGGER.info("Verification email sent to user {}", user.getId());
            emailService.sendVerificationEmail(user, verificationToken);
        }
        return "Registration successful. Please verify your email.";
    }

    public LoginResponseDTO login(LoginRequestDTO requestDTO) {
        LOGGER.info("Login request received for email: {}", requestDTO.getEmail());
        Authentication auth = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(
                        requestDTO.getEmail(),
                        requestDTO.getPassword())
                );
        LOGGER.debug("Authentication successful for email: {}", requestDTO.getEmail());
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        LoginResponseDTO responseDTO = LoginResponseDTO.builder()
                        .userId(userDetails.getId())
                        .email(userDetails.getUsername())
                        .primaryRole(userService.getHighestAuthRole(userService.getUserRoles(userDetails)))
                        .accessToken(jwtService.generateToken(userDetails))
                        .build();
        LOGGER.info("Login successful for user: {}", userDetails.getUsername());
        return responseDTO;
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
        LOGGER.info("Fetching user using token from password reset token: {}", token);
        PasswordResetToken resetToken = passwordResetTokenService.findUserFromToken(token);
        User user = resetToken.getUser();
        passwordResetTokenService.checkTokenExpiry(resetToken);
        String oldValue = user.getPassword();
        LOGGER.debug("User successfully loaded: {}", user.getName());
        if (passwordEncoder.matches(resetDTO.getNewPassword(), oldValue)) {
            LOGGER.warn("User attempted to reuse the existing password. userId={}", user.getId());
            throw new SamePasswordException("New password must be different from old password.");
        }
        user.setPassword(passwordEncoder.encode(resetDTO.getNewPassword()));
        LOGGER.info("Password reset successful for userId: {}", user.getId());
        passwordResetTokenService.deleteToken(resetToken);
        auditService.log(AuditEntityType.USER, user.getId(), AuditAction.UPDATE, "Password", oldValue, user.getPassword(), user.getId());
        return "Password reset successfully";
    }
}
