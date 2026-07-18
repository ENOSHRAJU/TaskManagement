package com.example.taskManagement.DTOs;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserAssignResponseDTO {
    private UUID userId;
    private String username;
    private UUID projectId;
    private String projectName;
    private String message;
}
