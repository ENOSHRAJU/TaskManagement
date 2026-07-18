package com.example.taskManagement.DTOs;

import com.example.taskManagement.Enums.TaskPriority;
import com.example.taskManagement.Enums.TaskStatus;
import com.example.taskManagement.Enums.TaskCategory;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
        "status",
        "taskCategory",
        "priority",
        "assignedTo",
        "createAt",
        "updatedAt"
})
public class TasksSummaryDTO {
    private UUID id;
    private String title;
    private TaskStatus status;
    private TaskCategory taskCategory;
    private TaskPriority taskPriority;
    private UserSummaryDTO assignedTo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDate dueDate;
}
