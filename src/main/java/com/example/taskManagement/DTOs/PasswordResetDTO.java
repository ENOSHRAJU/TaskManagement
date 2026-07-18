package com.example.taskManagement.DTOs;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO for resetting user password")
public class PasswordResetDTO {

    @Schema(
            description = "New password of the user (must contain letters, numbers and special characters)",
            example = "Pass@12345"
    )
    @NotBlank(message = "New password should not be empty")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@#$%^&+=])[A-Za-z\\d@#$%^&+=]{6,20}$",
            message = "Password must contain at least one letter, one number and one special character"
    )
    private String newPassword;
}