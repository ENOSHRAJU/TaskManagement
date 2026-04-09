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
        task.setPriority(requestDTO.getPriority());
        task.setProject(project);
        task.setTaskCategory(requestDTO.getTaskCategory());
        return task;
    }

    public static TaskResponseDTO toDTO (Task task) {
        TaskResponseDTO responseDTO = new TaskResponseDTO();
        responseDTO.setId(task.getId());
        responseDTO.setTitle(task.getTitle());
        responseDTO.setDescription(task.getDescription());
        responseDTO.setStatus(task.getStatus());
        responseDTO.setPriority(task.getPriority());
        responseDTO.setTaskCategory(task.getTaskCategory());
        if(task.getAssignedTo()!=null) {
            responseDTO.setAssignedTo(UserMapper.userSummaryDTO(task.getAssignedTo()));
        }
        responseDTO.setProjectId(task.getProject().getId());
        responseDTO.setCreatedAt(task.getCreatedAt());
        responseDTO.setUpdatedAt(task.getUpdatedAt());
        return responseDTO;
    }

    public static TasksSummaryDTO toSummaryDTO(Task task) {
        TasksSummaryDTO summaryDTO = new TasksSummaryDTO();
        summaryDTO.setId(task.getId());
        summaryDTO.setTitle(task.getTitle());
        summaryDTO.setStatus(task.getStatus());
        summaryDTO.setTaskCategory(task.getTaskCategory());
        summaryDTO.setPriority(task.getPriority());
        if(task.getAssignedTo() != null){
            summaryDTO.setAssignedTo(UserMapper.userSummaryDTO(task.getAssignedTo()));
        }
        summaryDTO.setCreatedAt(task.getCreatedAt());
        summaryDTO.setUpdatedAt(task.getUpdatedAt());
        return summaryDTO;
    }
}
