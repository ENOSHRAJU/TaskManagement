package com.example.taskManagement.DTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ForgotPasswordDTO {
    @NotNull(message = "Email should not be empty")
    @Email
    private String email;
}
