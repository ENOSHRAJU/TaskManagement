package com.example.taskManagement.DTOs;

import com.example.taskManagement.Enums.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TaskUpdateDTO {
    private String title;
    private String description;
    private TaskPriority taskPriority;
    private LocalDate dueDate;
}
