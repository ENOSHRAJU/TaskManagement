package com.example.taskManagement.Service;

import com.example.taskManagement.DTOs.TaskRequestDTO;
import com.example.taskManagement.DTOs.TaskResponseDTO;
import com.example.taskManagement.DTOs.TaskStatusUpdateDTO;
import com.example.taskManagement.DTOs.TaskUpdateDTO;
import com.example.taskManagement.Enums.TaskCategory;
import com.example.taskManagement.Enums.TaskPriority;
import com.example.taskManagement.Enums.TaskSortField;
import com.example.taskManagement.Enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

public interface TaskService {

    Page<TaskResponseDTO> getAllTasks(int page, int size, TaskStatus status, TaskCategory category, TaskPriority priority, TaskSortField sortBy, Sort.Direction direction, UUID assignedTo, String search, UUID projectId);
    TaskResponseDTO getTaskById(UUID id);
    Page<TaskResponseDTO> getTasksByProject(UUID projectId, int page, int size, TaskStatus status, TaskPriority priority, TaskCategory category, UUID assignedTo, String search, TaskSortField sortBy, Sort.Direction direction);
    TaskResponseDTO createTask(UUID projectId, TaskRequestDTO requestDTO);
    TaskResponseDTO updateTask(UUID projectId, UUID taskId, TaskUpdateDTO updateDTO);
    TaskResponseDTO assignUser(UUID projectId, UUID userId, UUID taskId);
    TaskResponseDTO updateTaskStatus(UUID projectId, UUID taskId, TaskStatusUpdateDTO taskStatusUpdateDTO);
    String deleteTask(UUID projectId, UUID taskId); //soft delete
    List<TaskResponseDTO> getOverdueTasksForUser(UUID userId);
    List<TaskResponseDTO> getAllOverdueTasks();

}