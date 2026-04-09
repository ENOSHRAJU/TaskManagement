package com.example.taskManagement.DTOs;

import com.example.taskManagement.Model.Category;
import com.example.taskManagement.Model.Priority;
import com.example.taskManagement.Model.Status;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

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
    private Long id;
    @NotNull(message = "Task title should not be null")
    private String title;
    @NotNull(message = "Task description should not be null")
    private String description;
    @NotNull(message = "Task status should not be null")
    private Status status;
    @NotNull(message = "Task priority should not be null")
    private Priority priority;
    @NotNull(message = "Task category should not be null")
    private Category taskCategory;
    private UserSummaryDTO assignedTo;
    @NotNull(message = "Task project should not be null")
    private long projectId;
    @NotNull(message = "Task createdAt should not be null")
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
