package com.example.taskManagement.DTOs;

import com.example.taskManagement.Model.Category;
import com.example.taskManagement.Model.Priority;
import com.example.taskManagement.Model.Status;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

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
    private Long id;
    private String title;
    private Status status;
    private Category taskCategory;
    private Priority priority;
    private UserSummaryDTO assignedTo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
