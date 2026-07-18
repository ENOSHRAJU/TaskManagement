package com.example.taskManagement.DTOs;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "DTO for registering new user")
public class RegisterDTO {

    @Schema(description = "Full name of the user", example = "Enosh raju")
    @NotBlank(message = "Name is required")
    private String name;

    @Schema(description = "User email address", example = "enosh@gmail.com")
    @NotBlank(message = "Email is required")
    @Email(message = "Email format is incorrect")
    private String email;

    @Schema(description = "User password (6-20) characters", example = "pass@12345")
    @NotBlank(message = "Password is required")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@#$%^&+=])[A-Za-z\\d@#$%^&+=]{6,20}$",
            message = "Password must contain at-least one special character, one letter and one number"
    )
    private String password;
}
