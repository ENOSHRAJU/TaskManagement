package com.example.taskManagement.Controllers;

import com.example.taskManagement.Common.ApiResponse;
import com.example.taskManagement.DTOs.TaskResponseDTO;
import com.example.taskManagement.Enums.TaskCategory;
import com.example.taskManagement.Enums.TaskPriority;
import com.example.taskManagement.Enums.TaskSortField;
import com.example.taskManagement.Enums.TaskStatus;
import com.example.taskManagement.Service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tasks")
@Tag(name = "Task Handler", description = "API's for task management operations")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
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

    @Operation(
            summary = "Get my overdue tasks",
            description = "Retrieves all overdue tasks assigned to a logged in user."
    )
    @GetMapping("/my/overdue")
    public ApiResponse<List<TaskResponseDTO>> getMyOverDueTasks() {
        return ApiResponse.success("Overdue tasks retrieved successfully",
                taskService.getMyOverDueTasks());
    }

    @Operation(
            summary = "Get overdue tasks for a specific user",
            description = "Admin and Manager only. Retrieves all overdue tasks assigned to a specific user by ID."
    )
    @GetMapping("/user/{userId}/overdue")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ApiResponse<List<TaskResponseDTO>> getOverdueTasksForUser(@PathVariable UUID userId) {
        return ApiResponse.success("Overdue tasks retrieved successfully",
                taskService.getOverdueTasksForUser(userId));
    }

    @Operation(
            summary = "Get all overdue tasks",
            description = "Admin only. Retrieves all overdue tasks across all projects."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/overdue")
    public ApiResponse<List<TaskResponseDTO>> getAllOverdueTasks() {
        return ApiResponse.success("All overdue tasks retrieved successfully",
                taskService.getAllOverdueTasks());
    }

}
