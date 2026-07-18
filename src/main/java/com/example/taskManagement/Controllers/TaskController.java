package com.example.taskManagement.Controllers;

import com.example.taskManagement.Common.ApiResponse;
import com.example.taskManagement.DTOs.TaskResponseDTO;
import com.example.taskManagement.DTOs.TaskStatusUpdateDTO;
import com.example.taskManagement.DTOs.TaskUpdateDTO;
import com.example.taskManagement.Enums.TaskCategory;
import com.example.taskManagement.Enums.TaskPriority;
import com.example.taskManagement.Enums.TaskSortField;
import com.example.taskManagement.Enums.TaskStatus;
import com.example.taskManagement.Service.TaskService;
import com.example.taskManagement.Service.TaskServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.PreUpdate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tasks")
@Tag(name = "Task Handler", description = "API's for task management operations")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskServiceImpl taskService) {
        this.taskService = taskService;
    }

    @Operation(
            summary = "Get all tasks present in the application",
            description = "Retrieves paginated tasks with optional filtering, searching, and sorting."
    )
    @GetMapping
    public ApiResponse<Page<TaskResponseDTO>> getAllTasks(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskCategory category,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) UUID assignedTo,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID projectId,
            @RequestParam(defaultValue = "CREATED_AT") TaskSortField sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
            ) {
        return ApiResponse.success("Successfully retrieved all the tasks in application",
                taskService.getAllTasks(page, size, status, category, priority, sortBy, direction, assignedTo, search, projectId));
    }

    @Operation(
            summary = "Get task by ID",
            description = "Retrieves the details of a specific task using its unique identifier."
    )
    @GetMapping("/{taskId}")
    public ApiResponse<TaskResponseDTO> getTaskById(@PathVariable UUID taskId) {
        return ApiResponse.success("Task retrieved successfully", taskService.getTaskById(taskId));
    }

    @GetMapping("/user/{userId}/overdue")
    public ResponseEntity<List<TaskResponseDTO>> getOverdueTasksForUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(taskService.getOverdueTasksForUser(userId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/overdue")
    public ResponseEntity<List<TaskResponseDTO>> getAllOverdueTasks() {
        return ResponseEntity.ok(taskService.getAllOverdueTasks());
    }

}
