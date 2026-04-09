package com.example.taskManagement.Service;

import com.example.taskManagement.DTOs.*;
import com.example.taskManagement.Model.ProjectStatus;

import java.util.List;

public interface ProjectService {

    List<ProjectResponseDTO> getAllProjects();
    ProjectResponseDTO getProjectById(Long projectId);
    List<ProjectResponseDTO> getProjectsByStatus(ProjectStatus status);
    List<ProjectResponseDTO> getProjectsByCreatedBy(Long userId);
    ProjectResponseDTO createProject(ProjectRequestDTO requestDTO);
    ProjectResponseDTO updateProject(ProjectUpdateDTO updateDTO);
    String deleteProject(Long projectId);
    ProjectResponseDTO addUserToProject(Long userId, Long projectId);
    ProjectResponseDTO removeUserFromProject(Long userId, Long projectId);
    List<UserSummaryDTO> getAllUsersByProject(Long projectId);

}


