package com.example.taskManagement.DTOs;

import com.example.taskManagement.Enums.ProjectStatus;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@JsonPropertyOrder({
        "projectId",
        "name",
        "description",
        "status",
        "createdAt",
        "createdBy",
        "tasks",
        "members"
})
public class ProjectResponseDTO {
    @NotNull(message = "project id should not be empty")
    private UUID projectId;
    @NotNull(message = "project name should not be empty")
    private String name;
    @NotNull(message = "project description should not be empty")
    private String description;
    @NotNull(message = "project status should not be empty")
    private ProjectStatus status;
    @NotNull(message = "project createdBy should not be empty")
    private UserSummaryDTO createdBy;
    @NotNull(message = "project createdAt should not be empty")
    private LocalDateTime createdAt;
    @NotNull(message = "project updatedAt should not be empty")
    private LocalDateTime updatedAt;
}
