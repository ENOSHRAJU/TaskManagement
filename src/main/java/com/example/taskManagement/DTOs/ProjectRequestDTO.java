package com.example.taskManagement.DTOs;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class ProjectRequestDTO {
    @NotNull(message = "project name should not be empty")
    private String name;
    @NotNull(message = "project description should not be empty")
    private String description;
}
