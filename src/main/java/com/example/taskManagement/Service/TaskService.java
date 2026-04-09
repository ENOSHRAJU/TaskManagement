package com.example.taskManagement.Service;

import com.example.taskManagement.DTOs.TaskRequestDTO;
import com.example.taskManagement.DTOs.TaskResponseDTO;
import com.example.taskManagement.DTOs.TaskStatusUpdateDTO;
import com.example.taskManagement.DTOs.TaskUpdateDTO;
import com.example.taskManagement.Model.Status;

import java.util.List;

public interface TaskService {

    List<TaskResponseDTO> getAllTasks();
    TaskResponseDTO getTaskById(Long id);
    List<TaskResponseDTO> getTasksByStatus(Status status, Long projectId);
    List<TaskResponseDTO> getTasksByProject(Long projectId);
    List<TaskResponseDTO> getTasksByUser(Long userId);
    TaskResponseDTO createTask(Long projectId, TaskRequestDTO requestDTO);
    TaskResponseDTO updateTask(Long taskId, TaskUpdateDTO updateDTO);
    TaskResponseDTO assignUser(Long userId, Long taskId);
    TaskResponseDTO updateTaskStatus(Long taskId, TaskStatusUpdateDTO taskStatusUpdateDTO);
    String deleteTask(Long taskId); //soft delete

}