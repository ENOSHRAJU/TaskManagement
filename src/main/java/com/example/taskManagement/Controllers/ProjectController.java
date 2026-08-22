package com.example.taskManagement.Controllers;

import com.example.taskManagement.Common.ApiResponse;
import com.example.taskManagement.DTOs.*;
import com.example.taskManagement.Enums.*;
import com.example.taskManagement.Service.ProjectService;
import com.example.taskManagement.Service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
@Tag(name="Project handler", description = "APIs for project CRUD operations")
public class ProjectController {

    private final ProjectService projectService;
    private final TaskService taskService;

    public ProjectController(ProjectService projectService, TaskService taskService) {
        this.projectService = projectService;
        this.taskService = taskService;
    }

    @Operation(
            summary = "Get all the projects",
            description = """
                        Retrieves projects visible to the current user with support for
                        pagination, searching, filtering, and sorting.
                        """
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ApiResponse<Page<ProjectSummaryDTO>> getAllProjects(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(0) @Max(100) int size,
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "CREATED_AT") ProjectSortField sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction,
            @RequestParam(required = false) UUID createdBy
            ) {
        return ApiResponse.success(
                "Retrieved all projects successfully",
                projectService.getAllProjects(page, size, status, search, sortBy, direction, createdBy)
        );
    }

    @Operation(
            summary = "Get project by ID",
            description = "Retrieve a project using its unique identifier"
    )
    @GetMapping("/{projectId}")
    public ApiResponse<ProjectResponseDTO> getProjectById(@PathVariable UUID projectId) {
        return ApiResponse.success(
                "Project retrieved successfully",
                projectService.getProjectById(projectId)
        );
    }

    @Operation(
            summary = "Get all the tasks of a project",
            description = "Retrieves paginated tasks for a project with optional filtering, searching and sorting."
    )
    @GetMapping("/{projectId}/tasks")
    public ApiResponse<Page<TaskResponseDTO>> getTasksByProject(
            @PathVariable UUID projectId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) TaskCategory category,
            @RequestParam(required = false) UUID assignedTo,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "CREATED_AT") TaskSortField sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
            ) {
        return ApiResponse.success(
                "Project tasks retrieved successfully",
                taskService.getTasksByProject(projectId, page, size, status, priority, category, assignedTo, search, sortBy, direction)
        );
    }

    @Operation(
            summary = "Get users of a project",
            description = "Retrieves paginated project members with optional search and sorting."
    )
    @GetMapping("/{projectId}/users")
    public ApiResponse<Page<UserSummaryDTO>> getAllUsersByProject(
            @PathVariable UUID projectId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "NAME") UserSortField sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction
    ) {
        return ApiResponse.success(
                "Project members retrieved successfully",
                projectService.getAllUsersByProject(projectId, page, size, search, sortBy, direction)
        );
    }

    @Operation(
            summary = "Creates project",
            description = "Allows admin and manager to create a new project"
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping
    public ApiResponse<ProjectCreatedResDTO> createProject(@RequestBody @Valid ProjectRequestDTO requestDTO){
        return ApiResponse.success(
                "Project created successfully",
                projectService.createProject(requestDTO)
        );
    }

    @Operation(
            summary = "Update project",
            description = "Allows admin and manager to update an existing project"
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PatchMapping("/{projectId}")
    public ApiResponse<ProjectResponseDTO> updateProject(
            @PathVariable UUID projectId,
            @RequestBody @Valid ProjectUpdateDTO projectUpdateDTO
    ) {
        return ApiResponse.success(
                "Updated project successfully",
                projectService.updateProject(projectId, projectUpdateDTO)
        );
    }

    @Operation(
            summary = "Creates task in project",
            description = "Allows admin and manager to create tasks in a project"
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping("/{projectId}/tasks")
    public ApiResponse<TaskResponseDTO> createTask(
            @PathVariable UUID projectId,
            @RequestBody @Valid TaskRequestDTO requestDTO
    ) {
        return ApiResponse.success(
                "Task created successfully",
                taskService.createTask(projectId, requestDTO)
        );
    }

    @Operation(
            summary = "Update task",
            description = "Allows managers and admins to update the details of an existing task."
    )
    @PatchMapping("{projectId}/tasks/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ApiResponse<TaskResponseDTO> updateTask(
            @PathVariable UUID projectId,
            @RequestBody @Valid TaskUpdateDTO taskUpdateDTO,
            @PathVariable UUID taskId) {
        return ApiResponse.success("Task updated successfully",
                taskService.updateTask(projectId,taskId, taskUpdateDTO));
    }

    @Operation(
            summary = "Update task status",
            description = "Updates the status of a task. Project members can change the status of tasks belonging to their project."
    )
    @PatchMapping("{projectId}/tasks/{taskId}/status")
    public ApiResponse<TaskResponseDTO> updateTaskStatus(
            @PathVariable UUID taskId,
            @RequestBody @Valid TaskStatusUpdateDTO taskStatusUpdateDTO,
            @PathVariable UUID projectId) {
        return ApiResponse.success("Task status was successfully updated" ,
                taskService.updateTaskStatus(projectId,taskId, taskStatusUpdateDTO));
    }

    @Operation(
            summary = "Delete a task",
            description = "Soft deletes a task. Only project owners (Managers) or Administrators can delete tasks."
    )
    @DeleteMapping("{projectId}/tasks/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<String> deleteTask(@PathVariable UUID projectId, @PathVariable UUID taskId) {
        return ApiResponse.success("Task deleted successfully",
                taskService.deleteTask(projectId, taskId));
    }

    @Operation(
            summary = "Assign a user to a task",
            description = "Allows project owners or project members to assign a project member to a task."
    )
    @PatchMapping("{projectId}/tasks/{taskId}/assignUser/{userId}")
    public ApiResponse<TaskResponseDTO> assignUser(
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @PathVariable UUID userId) {
        return ApiResponse.success("Task assigned to user successfully",
                taskService.assignUser(projectId, userId, taskId));
    }

    @Operation(
            summary = "Delete project",
            description = "Allows admin to soft delete a project"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{projectId}")
    public ApiResponse<String> deleteProject(@PathVariable UUID projectId) {
        return ApiResponse.success(
                "Project deleted successfully",
                projectService.deleteProject(projectId)
        );
    }

    @Operation(
            summary = "Adds user to a project",
            description = "Allows admin and manager to add a user to a project"
    )
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping("/{projectId}/users/{userId}")
    public ApiResponse<UserAssignResponseDTO> addUserToProject(
            @PathVariable UUID projectId,
            @PathVariable UUID userId
    ) {
        return ApiResponse.success(
                "User added to a project successfully",
                projectService.addUserToProject(userId, projectId)
        );
    }

    @Operation(
            summary = "Remove user from a project",
            description = "Allows admin and manager to remove a user from a project"
    )
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @DeleteMapping("/{projectId}/users/{userId}")
    public ApiResponse<ProjectResponseDTO> removeUserFromProject(
            @PathVariable UUID projectId,
            @PathVariable UUID userId
    ) {

        return ApiResponse.success(
                "User removed from project successfully",
                projectService.removeUserFromProject(userId, projectId)
        );

    }

}
