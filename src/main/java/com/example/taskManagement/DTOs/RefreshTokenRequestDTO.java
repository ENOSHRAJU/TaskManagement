package com.example.taskManagement.DTOs;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequestDTO {
    @Schema(
            description = "Refresh token issued during login",
            example = "d6d9c4b1-6b5e-4c5d-9d3a-6e2a3f8f4b9c"
    )
    private String refreshToken;
}
