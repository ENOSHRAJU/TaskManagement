package com.example.taskManagement.DTOs;

import com.example.taskManagement.Model.Priority;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TaskUpdateDTO {
    private String title;
    private String description;
    private Priority priority;
}
