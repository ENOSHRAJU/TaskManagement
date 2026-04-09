package com.example.taskManagement.Controllers;

import com.example.taskManagement.DTOs.TaskRequestDTO;
import com.example.taskManagement.DTOs.TaskResponseDTO;
import com.example.taskManagement.DTOs.TaskStatusUpdateDTO;
import com.example.taskManagement.DTOs.TaskUpdateDTO;
import com.example.taskManagement.Model.Status;
import com.example.taskManagement.Service.TaskServiceImpl;
import com.example.taskManagement.Service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private final TaskServiceImpl taskService;

    public TaskController(TaskServiceImpl taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<List<TaskResponseDTO>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponseDTO> getTaskById(@PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.getTaskById(taskId));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<TaskResponseDTO>> getTasksByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(taskService.getTasksByProject(projectId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TaskResponseDTO>> getTasksByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(taskService.getTasksByUser(userId));
    }

    @GetMapping("/user/{status}/projects/{projectId}")
    public ResponseEntity<List<TaskResponseDTO>> getTasksByStatus(
            @PathVariable Status status, @PathVariable Long projectId) {
        return ResponseEntity.ok(taskService.getTasksByStatus(status, projectId));
    }

    @PatchMapping("/update/{taskId}")
    public ResponseEntity<TaskResponseDTO> updateTask(@RequestBody @Valid TaskUpdateDTO taskUpdateDTO, @PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.updateTask(taskId, taskUpdateDTO));
    }

    @PatchMapping("/{taskId}/assignUser/{userId}")
    public ResponseEntity<TaskResponseDTO> assignUser(@PathVariable Long taskId, @PathVariable Long userId) {
        return ResponseEntity.ok(taskService.assignUser(userId, taskId));
    }

    @PatchMapping("/{taskId}/status/project/{projectId}")
    public ResponseEntity<TaskResponseDTO> updateTaskStatus(
            @PathVariable Long taskId,
            @RequestBody @Valid TaskStatusUpdateDTO taskStatusUpdateDTO,
            @PathVariable Long projectId) {
        return ResponseEntity.ok(taskService.updateTaskStatus(taskId, taskStatusUpdateDTO));
    }

    @PreAuthorize("hasAnyRole('ADMIN',MANAGER')")
    @DeleteMapping("/{taskId}")
    public ResponseEntity<String> deleteTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.deleteTask(taskId));
    }
}
