package com.example.taskManagement.DTOs;

import com.example.taskManagement.Enums.TaskPriority;
import com.example.taskManagement.Enums.TaskCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;


@Getter
@Setter
public class TaskRequestDTO {
    @NotBlank(message = "Task title should not be empty")
    private String title;
    @NotBlank(message = "Task description should not be empty")
    private String description;
    @NotNull(message = "Priority should not be empty")
    private TaskPriority taskPriority;
    @NotNull(message = "Task category should not be empty")
    private TaskCategory taskCategory;
    @NotNull(message = "Task due date should not be empty")
    private LocalDate dueDate;
}
