package com.example.taskManagement.Controllers;

import com.example.taskManagement.DTOs.*;
import com.example.taskManagement.Model.ProjectStatus;
import com.example.taskManagement.Service.ProjectService;
import com.example.taskManagement.Service.ProjectServiceImpl;
import com.example.taskManagement.Service.TaskService;
import com.example.taskManagement.Service.TaskServiceImpl;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    private final ProjectServiceImpl projectService;
    private final TaskServiceImpl taskService;

    public ProjectController(ProjectServiceImpl projectService, TaskServiceImpl taskService) {
        this.projectService = projectService;
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponseDTO>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponseDTO> getProjectById(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectService.getProjectById(projectId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping("/create")
    public ResponseEntity<ProjectResponseDTO> createProject(@RequestBody @Valid ProjectRequestDTO requestDTO){
        return ResponseEntity.ok(projectService.createProject(requestDTO));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PatchMapping("/update")
    public ResponseEntity<ProjectResponseDTO> updateProject(@RequestBody @Valid ProjectUpdateDTO projectUpdateDTO) {
        return ResponseEntity.ok(projectService.updateProject(projectUpdateDTO));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TEAM_LEAD')")
    @PostMapping("/{projectId}/createTask")
    public ResponseEntity<TaskResponseDTO> createTask(@PathVariable Long projectId, @RequestBody @Valid TaskRequestDTO requestDTO) {
        return ResponseEntity.ok(taskService.createTask(projectId, requestDTO));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{projectId}")
    public ResponseEntity<String> deleteProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectService.deleteProject(projectId));
    }

    @GetMapping("{projectId}/tasks")
    public ResponseEntity<List<TaskResponseDTO>> getTasksByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(taskService.getTasksByProject(projectId));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping("/{projectId}/users/{userId}")
    public ResponseEntity<ProjectResponseDTO> addUserToProject(@PathVariable Long projectId, @PathVariable Long userId) {
        return ResponseEntity.ok(projectService.addUserToProject(userId, projectId));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @DeleteMapping("/{projectId}/user/{userId}")
    public ResponseEntity<ProjectResponseDTO> removeUserFromProject(@PathVariable Long projectId, @PathVariable Long userId) {
        return ResponseEntity.ok(projectService.removeUserFromProject(userId, projectId));
    }

    @GetMapping("{projectId}/users")
    public ResponseEntity<List<UserSummaryDTO>> getAllUsersByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectService.getAllUsersByProject(projectId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ProjectResponseDTO>> getProjectsByStatus(@PathVariable ProjectStatus status) {
        return ResponseEntity.ok(projectService.getProjectsByStatus(status));
    }

    @GetMapping("/created-by/{userId}")
    public ResponseEntity<List<ProjectResponseDTO>> getProjectsByCreatedBy(@PathVariable Long userId) {
        return ResponseEntity.ok(projectService.getProjectsByCreatedBy(userId));
    }

}
