package com.example.taskManagement.DTOs;

import com.example.taskManagement.Model.ProjectStatus;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@JsonPropertyOrder({
        "id",
        "name",
        "description",
        "status",
        "createdBy",
        "createdAt",
        "totalTask",
        "totalMembers"
})
public class ProjectSummaryDTO {
    @NotNull(message = "Project id should not be empty")
    private Long id;
    @NotNull(message = "Project name should not be empty")
    private String name;
    @NotNull(message = "Project description should not be empty")
    private String description;
    @NotNull(message = "Project status should not be empty")
    private ProjectStatus status;
    @NotNull(message = "Project createdBy should not be empty")
    private UserSummaryDTO createdBy;
    @NotNull(message = "Project createdAT should not be empty")
    private LocalDateTime createdAt;
    @NotNull(message = "Project totalTask should not be empty")
    private Long totalTask;
    @NotNull(message = "Project totalMembers should not be empty")
    private Long totalMembers;
}
