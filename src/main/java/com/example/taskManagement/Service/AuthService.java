package com.example.taskManagement.Service;

import com.example.taskManagement.Common.ApiResponse;
import com.example.taskManagement.Configurations.JwtService;
import com.example.taskManagement.DTOs.LoginRequestDTO;
import com.example.taskManagement.DTOs.LoginResponseDTO;
import com.example.taskManagement.DTOs.PasswordResetDTO;
import com.example.taskManagement.DTOs.RegisterDTO;
import com.example.taskManagement.Exception.DuplicateEmailException;
import com.example.taskManagement.Model.RoleTypes;
import com.example.taskManagement.Model.User;
import com.example.taskManagement.Repository.RoleRepository;
import com.example.taskManagement.Repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public ApiResponse<Void> registerUser(RegisterDTO registerDTO) {
        User user = new User();
        if(userRepository.existsByEmail(registerDTO.getEmail())) {
            LOGGER.warn("Registration failed: Email already exists: {}", registerDTO.getEmail());
            throw new DuplicateEmailException("Email already exists");
        }
        user.setName(registerDTO.getName());
        user.setEmail(registerDTO.getEmail());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.getRoles().add(roleRepository.findByRole(RoleTypes.USER.toString())
                .orElseThrow(() -> new RuntimeException("Role not found")));
        userRepository.save(user);
        LOGGER.info("User registered successfully userId: {}, email: {}", user.getId(), user.getEmail());
        return new ApiResponse<>(200, "registered successfully", null);
    }

    public ApiResponse<LoginResponseDTO> login(LoginRequestDTO requestDTO) {
        Authentication auth = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(
                        requestDTO.getEmail(),
                        requestDTO.getPassword())
                );

        UserDetails userDetails = (UserDetails)auth.getPrincipal();
        String token = jwtService.generateToken(userDetails);
        LOGGER.info("Login successful for email={}", userDetails.getUsername());
        return new ApiResponse<>(200, "Login successful", new LoginResponseDTO(token));
    }

    public ApiResponse<Void> handleForgotPassword(String username) {
        if(!userRepository.existsByEmail(username)) {
            LOGGER.warn("Forgot password requested for non-existing email={}", username);
            throw new UsernameNotFoundException("Username not found with email: "+ username);
        }
        LOGGER.info("Forgot password request accepted for email={}", username);
        return new ApiResponse<>(200, "Username found", null);
    }

    public ApiResponse<Void> handleResetPassword(PasswordResetDTO resetDTO) {
        User user = userRepository.findByEmail(resetDTO.getEmail())
                .orElseThrow(() -> {
                    LOGGER.warn("Password reset failed: email not found={}", resetDTO.getEmail());
                    return new UsernameNotFoundException("Username not found with email: "+ resetDTO.getEmail());
                });
        user.setPassword(passwordEncoder.encode(resetDTO.getNewPassword()));
        userRepository.save(user);
        LOGGER.info("Password reset successful for userId: {}", user.getId());
        return new ApiResponse<>(200, "Password reset successful", null);
    }
}
