package com.example.taskManagement.DTOs;

import com.example.taskManagement.Model.Category;
import com.example.taskManagement.Model.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class TaskRequestDTO {
    @NotBlank(message = "Task title should not be empty")
    private String title;
    @NotBlank(message = "Task description should not be empty")
    private String description;
    @NotNull(message = "Priority should not be empty")
    private Priority priority;
    @NotNull(message = "Task category should not be empty")
    private Category taskCategory;
}
