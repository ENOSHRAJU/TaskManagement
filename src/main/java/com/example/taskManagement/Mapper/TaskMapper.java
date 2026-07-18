package com.example.taskManagement.Mapper;

import com.example.taskManagement.DTOs.TaskRequestDTO;
import com.example.taskManagement.DTOs.TaskResponseDTO;
import com.example.taskManagement.DTOs.TasksSummaryDTO;
import com.example.taskManagement.Model.Project;
import com.example.taskManagement.Model.Task;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public static Task toEntity (TaskRequestDTO requestDTO, Project project) {
        Task task = new Task();
        task.setTitle(requestDTO.getTitle());
        task.setDescription(requestDTO.getDescription());
        task.setTaskPriority(requestDTO.getTaskPriority());
        task.setProject(project);
        task.setTaskCategory(requestDTO.getTaskCategory());
        task.setDueDate(requestDTO.getDueDate());
        return task;
    }

    public static TaskResponseDTO toDTO (Task task) {
        TaskResponseDTO responseDTO = new TaskResponseDTO();
        responseDTO.setId(task.getId());
        responseDTO.setTitle(task.getTitle());
        responseDTO.setDescription(task.getDescription());
        responseDTO.setStatus(task.getStatus());
        responseDTO.setTaskPriority(task.getTaskPriority());
        responseDTO.setTaskCategory(task.getTaskCategory());
        if(task.getAssignedTo()!=null) {
            responseDTO.setAssignedTo(UserMapper.userSummaryDTO(task.getAssignedTo()));
        }
        responseDTO.setProjectId(task.getProject().getId());
        responseDTO.setCreatedAt(task.getCreatedAt());
        responseDTO.setUpdatedAt(task.getUpdatedAt());
        responseDTO.setDueDate(task.getDueDate());
        return responseDTO;
    }

    public static TasksSummaryDTO toSummaryDTO(Task task) {
        TasksSummaryDTO summaryDTO = new TasksSummaryDTO();
        summaryDTO.setId(task.getId());
        summaryDTO.setTitle(task.getTitle());
        summaryDTO.setStatus(task.getStatus());
        summaryDTO.setTaskCategory(task.getTaskCategory());
        summaryDTO.setTaskPriority(task.getTaskPriority());
        if(task.getAssignedTo() != null){
            summaryDTO.setAssignedTo(UserMapper.userSummaryDTO(task.getAssignedTo()));
        }
        summaryDTO.setCreatedAt(task.getCreatedAt());
        summaryDTO.setUpdatedAt(task.getUpdatedAt());
        summaryDTO.setDueDate(task.getDueDate());
        return summaryDTO;
    }
}
