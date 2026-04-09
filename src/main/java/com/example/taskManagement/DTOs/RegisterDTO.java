package com.example.taskManagement.DTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterDTO {
    @NotBlank(message = "Name should not be null")
    private String name;
    @NotBlank(message = "Email should not be null")
    @Email(message = "Email format is incorrect")
    private String email;
    @NotBlank(message = "Password should not be null")
    @Size(min = 6, max = 20, message = "Password must have 6 to 18 characters")
    private String password;
}
