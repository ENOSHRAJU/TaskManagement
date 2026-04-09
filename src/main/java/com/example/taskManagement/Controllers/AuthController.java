package com.example.taskManagement.Controllers;

import com.example.taskManagement.Common.ApiResponse;
import com.example.taskManagement.DTOs.*;
import com.example.taskManagement.Service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<Void> registerUser(@RequestBody @Valid RegisterDTO registerDTO) {
        return authService.registerUser(registerDTO);
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponseDTO> login(@RequestBody  @Valid LoginRequestDTO requestDTO) {
        return authService.login(requestDTO);
    }

    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@RequestBody @Valid ForgotPasswordDTO forgotPasswordDTO) {
        return authService.handleForgotPassword(forgotPasswordDTO.getEmail());
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@RequestBody @Valid PasswordResetDTO resetDTO) {
        return authService.handleResetPassword(resetDTO);
    }
}
