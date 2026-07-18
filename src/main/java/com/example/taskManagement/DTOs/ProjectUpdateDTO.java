package com.example.taskManagement.DTOs;

import com.example.taskManagement.Enums.ProjectStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectUpdateDTO {
    private String name;
    private String description;
    private ProjectStatus status;
}
