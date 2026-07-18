package com.example.taskManagement.DTOs;

import com.example.taskManagement.Enums.TaskPriority;
import com.example.taskManagement.Enums.TaskStatus;
import com.example.taskManagement.Enums.TaskCategory;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@JsonPropertyOrder({
        "id",
        "title",
        "description",
        "status",
        "priority",
        "taskCategory",
        "user",
        "project",
        "createAt",
        "updatedAt"

})
public class TaskResponseDTO {
    @NotNull(message = "Task id should not be null")
    private UUID id;
    @NotNull(message = "Task title should not be null")
    private String title;
    @NotNull(message = "Task description should not be null")
    private String description;
    @NotNull(message = "Task status should not be null")
    private TaskStatus status;
    @NotNull(message = "Task priority should not be null")
    private TaskPriority taskPriority;
    @NotNull(message = "Task category should not be null")
    private TaskCategory taskCategory;
    private UserSummaryDTO assignedTo;
    @NotNull(message = "Task project should not be null")
    private UUID projectId;
    @NotNull(message = "Task createdAt should not be null")
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @NotNull(message = "Task dueDate should not be null")
    private LocalDate dueDate;
}
