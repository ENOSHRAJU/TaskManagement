package com.example.taskManagement.DTOs;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RefreshTokenResponseDTO {
    @Schema(
            description = "JWT access token for authenticated user",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    )
    private String accessToken;
    @Schema(
            description = "Refresh token issued during login",
            example = "d6d9c4b1-6b5e-4c5d-9d3a-6e2a3f8f4b9c"
    )
    private String refreshToken;
}
