package com.example.taskManagement.DTOs;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class TaskAssignUserDTO {
    private UUID userId;
}
