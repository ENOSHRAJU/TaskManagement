package com.example.taskManagement.DTOs;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO for forgot password")
public class ForgotPasswordDTO {

    @Schema(description = "Email of the user to reset password", example = "enosh@gmail.com")
    @NotBlank(message = "Email should not be empty")
    @Email(message = "Invalid email format")
    private String email;

}
