package com.example.taskManagement.DTOs;

import com.example.taskManagement.Enums.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskStatusUpdateDTO {
    @NotNull
    private TaskStatus status;
}
