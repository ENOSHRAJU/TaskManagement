package com.example.taskManagement.Service;

import com.example.taskManagement.DTOs.*;
import com.example.taskManagement.Enums.ProjectSortField;
import com.example.taskManagement.Enums.ProjectStatus;
import com.example.taskManagement.Enums.UserSortField;
import com.example.taskManagement.Model.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

public interface ProjectService {

    Project findProjectById(UUID projectId);
    Page<ProjectSummaryDTO> getAllProjects(int page, int size, ProjectStatus status, String search, ProjectSortField sortBy, Sort.Direction direction, UUID userId);
    ProjectResponseDTO getProjectById(UUID projectId);
    Page<UserSummaryDTO> getAllUsersByProject(UUID projectId, int page, int size, String search, UserSortField sortBy, Sort.Direction direction);
    ProjectCreatedResDTO createProject(ProjectRequestDTO requestDTO);
    ProjectResponseDTO updateProject(UUID projectId, ProjectUpdateDTO updateDTO);
    String deleteProject(UUID projectId);
    UserAssignResponseDTO addUserToProject(UUID userId, UUID projectId);
    ProjectResponseDTO removeUserFromProject(UUID userId, UUID projectId);

}


