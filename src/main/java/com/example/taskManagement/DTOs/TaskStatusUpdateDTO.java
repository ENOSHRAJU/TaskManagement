package com.example.taskManagement.DTOs;

import com.example.taskManagement.Model.Status;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskStatusUpdateDTO {
    @NotNull
    private Status status;
}
