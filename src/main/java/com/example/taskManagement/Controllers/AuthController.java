package com.example.taskManagement.Controllers;

import com.example.taskManagement.Common.ApiResponse;
import com.example.taskManagement.DTOs.*;
import com.example.taskManagement.Service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
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
            summary = "Verify email address",
            description = "Verifies a user's email address using the verification token sent during registration."
    )
    @GetMapping("/verify-email")
    public ApiResponse<String> verifyEmail(
            @Parameter(description = "Email verification token", required = true)
            @RequestParam String token) {
        return ApiResponse.success("Email verified successfully", authService.verifyEmail(token));
    }

    @Operation(
            summary = "Refresh access token",
            description = "Validates the provided refresh token and issues a new JWT access token if the refresh token is valid and not expired."
    )
    @PostMapping("/refresh-token")
    public ApiResponse<RefreshTokenResponseDTO> refreshToken(@RequestBody @Valid RefreshTokenRequestDTO request) {
        return ApiResponse.success("Access token refreshed successfully", authService.refreshToken(request.getRefreshToken()));
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

    @Operation(
            summary = "Logout user",
            description = "Logs out the authenticated user by invalidating the refresh token."
    )
    @PostMapping("/logout")
    public ApiResponse<String> logout(Authentication authentication) {
        return ApiResponse.success(
                "User logged out successfully",
                authService.logout(authentication)
        );
    }

}
