package com.example.taskManagement.DTOs;

import com.example.taskManagement.Model.ProjectStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectUpdateDTO {
    @NotNull(message = "Project id should not be empty")
    private Long id;
    private String name;
    private String description;
    private ProjectStatus status;
}
