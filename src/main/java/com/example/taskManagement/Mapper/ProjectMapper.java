package com.example.taskManagement.Mapper;

import com.example.taskManagement.DTOs.ProjectRequestDTO;
import com.example.taskManagement.DTOs.ProjectResponseDTO;
import com.example.taskManagement.DTOs.ProjectSummaryDTO;
import com.example.taskManagement.Model.Project;
import org.springframework.stereotype.Component;

import java.util.List;


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
        responseDTO.setTasks(
                project.getTasks() == null ? List.of() :
                project.getTasks().stream()
                        .map(TaskMapper::toSummaryDTO)
                        .toList()
        );
        responseDTO.setMembers(
                project.getMembers() == null ? List.of() :
                project.getMembers().stream()
                        .map(UserMapper::userSummaryDTO)
                        .toList()
        );
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

        if (project.getTasks() != null) summaryDTO.setTotalTask(project.getTasks().stream().count());
        else summaryDTO.setTotalTask(0L);

        if(project.getMembers() != null) summaryDTO.setTotalMembers(project.getMembers().stream().count());
        else summaryDTO.setTotalMembers(0L);
        return summaryDTO;
    }
}
