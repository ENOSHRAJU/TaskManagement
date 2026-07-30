package com.example.taskManagement.Controllers;

import com.example.taskManagement.Common.ApiResponse;
import com.example.taskManagement.DTOs.*;
import com.example.taskManagement.Service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
@Tag(name ="Authentication", description = "API's for user authentication and authorisation")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
            summary = "Register new account",
            description = "Creates a new user account in the system"
    )
    @PostMapping("/register")
    public ApiResponse<String> registerUser(@RequestBody @Valid RegisterDTO registerDTO) {
        return ApiResponse.success("Verification email sent successfully", authService.registerUser(registerDTO));
    }

    @Operation(
            summary = "User login",
            description = "Authenticate user using email and password and return JWT token"
    )
    @PostMapping("/login")
    public ApiResponse<LoginResponseDTO> login(@RequestBody  @Valid LoginRequestDTO requestDTO) {
        return ApiResponse.success("User logged in successfully", authService.login(requestDTO));
    }

    @Operation(
            summary = "Forgot password",
            description = "Checks whether user exists and initiates password reset process"
    )
    @PostMapping("/forgot-password")
    public ApiResponse<String> forgotPassword(@RequestBody @Valid ForgotPasswordDTO forgotPasswordDTO) {
        return ApiResponse.success("Password reset request processed successfully",
                authService.handleForgotPassword(forgotPasswordDTO.getEmail()));
    }

    @Operation(
            summary = "Reset password",
            description = "Resets user password using new password"
    )
    @PostMapping("/reset-password")
    public ApiResponse<String> resetPassword(@RequestParam("token") String token,
                                           @RequestBody @Valid PasswordResetDTO resetDTO) {
        return ApiResponse.success("Password reset successful",
                authService.handleResetPassword(token, resetDTO));
    }

}
