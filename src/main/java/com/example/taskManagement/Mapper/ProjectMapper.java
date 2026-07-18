package com.example.taskManagement.Mapper;

import com.example.taskManagement.DTOs.ProjectCreatedResDTO;
import com.example.taskManagement.DTOs.ProjectRequestDTO;
import com.example.taskManagement.DTOs.ProjectResponseDTO;
import com.example.taskManagement.DTOs.ProjectSummaryDTO;
import com.example.taskManagement.Model.Project;
import org.springframework.stereotype.Component;


@Component
public class ProjectMapper {

    public static ProjectResponseDTO toDTO(Project project) {
        ProjectResponseDTO responseDTO = new ProjectResponseDTO();
        responseDTO.setProjectId(project.getId());
        responseDTO.setName(project.getName());
        responseDTO.setDescription(project.getDescription());
        responseDTO.setStatus(project.getStatus());
        responseDTO.setCreatedBy(UserMapper.userSummaryDTO(project.getCreatedBy()));
        responseDTO.setCreatedAt(project.getCreatedAt());
        responseDTO.setUpdatedAt(project.getUpdatedAt());
        return responseDTO;
    }

    public static ProjectCreatedResDTO toCreateDTO(Project project) {
        ProjectCreatedResDTO responseDTO = new ProjectCreatedResDTO();
        responseDTO.setProjectId(project.getId());
        responseDTO.setName(project.getName());
        responseDTO.setDescription(project.getDescription());
        responseDTO.setStatus(project.getStatus());
        responseDTO.setCreatedBy(UserMapper.userSummaryDTO(project.getCreatedBy()));
        responseDTO.setCreatedAt(project.getCreatedAt());
        return responseDTO;
    }

    public static Project toEntity(ProjectRequestDTO requestDTO) {
        Project project = new Project();
        project.setName(requestDTO.getName());
        project.setDescription(requestDTO.getDescription());
        return project;
    }

    public static ProjectSummaryDTO toSummaryDTO(Project project) {
        ProjectSummaryDTO summaryDTO = new ProjectSummaryDTO();
        summaryDTO.setId(project.getId());
        summaryDTO.setName(project.getName());
        summaryDTO.setDescription(project.getDescription());
        summaryDTO.setStatus(project.getStatus());
        summaryDTO.setCreatedBy(UserMapper.userSummaryDTO(project.getCreatedBy()));
        summaryDTO.setCreatedAt(project.getCreatedAt());
        if (project.getTasks() != null) summaryDTO.setTotalTasks((long)project.getTasks().size());
        else summaryDTO.setTotalTasks(0L);
        if(project.getMembers() != null) summaryDTO.setTotalMembers((long)project.getMembers().size());
        else summaryDTO.setTotalMembers(0L);
        return summaryDTO;
    }
}
