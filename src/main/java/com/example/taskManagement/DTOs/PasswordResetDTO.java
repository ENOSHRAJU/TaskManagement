package com.example.taskManagement.DTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PasswordResetDTO {
    @NotNull(message = "username should not be empty")
    @Email
    private String email;
    @NotNull(message = "newPass should not be empty")
    private String newPassword;
}
