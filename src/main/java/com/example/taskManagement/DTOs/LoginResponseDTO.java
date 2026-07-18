package com.example.taskManagement.DTOs;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "DTO for login response with JWT token")
public class LoginResponseDTO {

    @Schema(
            description = "User Id",
            example= "12345"
    )
    private UUID userId;
    @Schema(
            description = "User email",
            example= "enoshraj76@gmail.com"
    )
    private String email;
    @Schema(
            description = "User primary role",
            example= "ADMIN"
    )
    private String primaryRole;
    @Schema(
            description = "JWT access token for authenticated user",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    )
    private String accessToken;
}
