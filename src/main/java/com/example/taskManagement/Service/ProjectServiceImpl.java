package com.example.taskManagement.Service;

import com.example.taskManagement.DTOs.*;
import com.example.taskManagement.Exception.ProjectNotFound;
import com.example.taskManagement.Mapper.ProjectMapper;
import com.example.taskManagement.Mapper.UserMapper;
import com.example.taskManagement.Model.Project;
import com.example.taskManagement.Model.ProjectStatus;
import com.example.taskManagement.Model.User;
import com.example.taskManagement.Repository.ProjectRepository;
import com.example.taskManagement.Repository.TaskRepository;
import com.example.taskManagement.Repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectServiceImpl implements ProjectService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectServiceImpl.class);
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository, UserRepository userRepository, TaskRepository taskRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    public List<ProjectResponseDTO> getAllProjects() {
        LOGGER.info("Fetching all the data");
        return projectRepository.findByStatusNot(ProjectStatus.CANCELLED).stream()
                .map(ProjectMapper::toDTO)
                .toList();
    }

    @Override
    public ProjectResponseDTO getProjectById(Long projectId) {
        LOGGER.info("Finding project with id: {}", projectId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFound("Project not found with id: "+ projectId));
        if(project.getStatus() == ProjectStatus.CANCELLED) throw new ProjectNotFound("Tried to access deleted project with id: "+ projectId);
        LOGGER.info("Found project with id: {}", projectId);
        return ProjectMapper.toDTO(project);
    }

    @Override
    public List<ProjectResponseDTO> getProjectsByStatus(ProjectStatus status) {
        LOGGER.info("Fetching all project with status: {}", status);
        return projectRepository.findByStatus(status).stream()
                .map(ProjectMapper::toDTO)
                .toList();
    }

    @Override
    public List<ProjectResponseDTO> getProjectsByCreatedBy(Long userId) {
        LOGGER.info("Fetching all projects with createdBy: {}", userId);
        return projectRepository.findByCreatedBy_IdAndStatusNot(userId, ProjectStatus.CANCELLED).stream()
                .map(ProjectMapper::toDTO)
                .toList();
    }

    @Override
    public List<UserSummaryDTO> getAllUsersByProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFound("Project not found with id: "+ projectId));
        return project.getMembers().stream()
                .map(UserMapper::userSummaryDTO)
                .toList();
    }

    @Override
    public ProjectResponseDTO createProject(ProjectRequestDTO requestDTO) {
        Project project = ProjectMapper.toEntity(requestDTO);
        LOGGER.info("Creating project with id: {}", project.getName());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        LOGGER.info("Getting username from security context");
        User user = userRepository.findByEmail(userName)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: "+ userName));
        project.setCreatedBy(user);
        projectRepository.save(project);
        LOGGER.info("Project created with id: {}", project.getId());
        return ProjectMapper.toDTO(project);
    }

    @Override
    public ProjectResponseDTO updateProject(ProjectUpdateDTO updateDTO) {
        LOGGER.info("Finding project to update with id: {}", updateDTO.getId());
        Project project = projectRepository.findById(updateDTO.getId())
                .orElseThrow(() -> new ProjectNotFound("Project not found with id: "+ updateDTO.getId()));
        LOGGER.info("Found project for updating with id: {}", project.getId());
        if(updateDTO.getName() != null) project.setName(updateDTO.getName());
        if(updateDTO.getDescription() != null) project.setDescription(updateDTO.getDescription());
        if(updateDTO.getStatus() != null) project.setStatus(updateDTO.getStatus());
        LOGGER.info("Project updated and saved successfully with id: {}", project.getId());
        projectRepository.save(project);
        return ProjectMapper.toDTO(project);
    }

    @Override
    public String deleteProject(Long projectId) {
        LOGGER.info("Finding project to delete with id: {}", projectId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFound("Project not found with id:"+ projectId));
        if(project.getStatus() == ProjectStatus.CANCELLED) throw new ProjectNotFound("Project was already deleted with id: "+ project);
        LOGGER.info("Found project to delete with id: {}", projectId);
        project.setStatus(ProjectStatus.CANCELLED);
        LOGGER.info("Deleted project successfully with id: {}", projectId);
        projectRepository.save(project);
        return "Project deleted successfully";
    }

    @Override
    public ProjectResponseDTO addUserToProject(Long userId, Long projectId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: "+ userId));
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFound("Project not found with id: "+ projectId));
        if(user.getProject() != null) throw new IllegalStateException("User is already present in a project with id: "+ user.getProject().getId());
        user.setProject(project);
        userRepository.save(user);
        return ProjectMapper.toDTO(project);
    }

    @Override
    public ProjectResponseDTO removeUserFromProject(Long userId, Long projectId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: "+ userId));
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFound("Project not found with id: "+ projectId));
        project.getMembers().remove(user);
        user.setProject(null);
        userRepository.save(user);
        projectRepository.save(project);
        return ProjectMapper.toDTO(project);
    }

}
