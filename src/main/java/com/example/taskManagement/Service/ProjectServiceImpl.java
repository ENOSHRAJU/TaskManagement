package com.example.taskManagement.Service;

import com.example.taskManagement.Configurations.AuthorizationService;
import com.example.taskManagement.Configurations.SecurityUtil;
import com.example.taskManagement.DTOs.*;
import com.example.taskManagement.Enums.*;
import com.example.taskManagement.Exception.*;
import com.example.taskManagement.Mapper.ProjectMapper;
import com.example.taskManagement.Mapper.UserMapper;
import com.example.taskManagement.Model.*;
import com.example.taskManagement.Repository.ProjectRepository;
import com.example.taskManagement.Repository.TaskRepository;
import com.example.taskManagement.Repository.UserRepository;
import com.example.taskManagement.Specifications.ProjectSpecification;
import com.example.taskManagement.Specifications.UserSpecification;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class ProjectServiceImpl implements ProjectService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectServiceImpl.class);
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final UserService userService;
    private final AuthorizationService authorizationService;
    private final SecurityUtil securityUtil;
    private final AuditService auditService;

    public ProjectServiceImpl(ProjectRepository projectRepository, UserRepository userRepository, TaskRepository taskRepository, UserService userService, SecurityUtil securityUtil, AuthorizationService authorizationService, AuditService auditService) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.userService = userService;
        this.authorizationService = authorizationService;
        this.securityUtil = securityUtil;
        this.auditService = auditService;
    }

    @Override
    public Project findProjectById(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> {
                    LOGGER.warn("Project not found with id: "+ projectId);
                    return new ProjectNotFound("Project not found with id: "+ projectId);
                });
    }

    @Override
    public Page<ProjectSummaryDTO> getAllProjects(
            int page, int size,
            ProjectStatus status,
            String search,
            ProjectSortField sortBy,
            Sort.Direction direction,
            UUID createdBy
    ) {
        LOGGER.info(
                "Admin fetching projects | page: {}, size: {}, status: {}, search: {}, sortBy: {}, direction: {}, userId: {}",
                page, size, status, search, sortBy, direction, createdBy
        );
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy.getField()));
        UUID userId = securityUtil.getCurrentUserDetails().getId();
        Specification<Project> specification = ProjectSpecification.accessibleProjects(securityUtil.isAdmin(), securityUtil.isManager(), userId)
                .and(ProjectSpecification.hasStatus(status))
                .and(ProjectSpecification.containsSearch(search))
                .and(ProjectSpecification.projectCreatedBy(createdBy));

        Page<Project> projects = projectRepository.findAll(specification, pageable);
        return projects.map(ProjectMapper::toSummaryDTO);
    }

    @Override
    public ProjectResponseDTO getProjectById(UUID projectId) {
        LOGGER.info("Finding project with id: {}", projectId);
        Project project = findProjectById(projectId);
        authorizationService.validateProjectAccess(project);
        LOGGER.info("User {} retrieved project {}", securityUtil.getCurrentUserDetails().getId(), projectId);
        return ProjectMapper.toDTO(project);
    }

    @Override
    public List<ProjectResponseDTO> getProjectsByStatus(ProjectStatus status) {
        UUID userId = securityUtil.getCurrentUserDetails().getId();
        List<Project> projects;
        if(securityUtil.isAdmin()) {
            LOGGER.info("Admin {} fetching all projects with status: {}",userId, status);
            projects = projectRepository.findByStatus(status);
        } else if(securityUtil.isManager()) {
            LOGGER.info("Manager {} fetching all project with status: {}",userId, status);
            projects = projectRepository.findByCreatedBy_IdAndStatus(userId, status);
        } else {
            LOGGER.info("User {} fetching all project with status: {}",userId, status);
            projects = projectRepository.findByMembers_IdAndStatus(userId, status);
        }
        return projects.stream()
                .map(ProjectMapper::toDTO)
                .toList();
    }

    @Override
    public List<ProjectResponseDTO> getProjectsByCreatedBy(UUID userId) {
        LOGGER.info("Fetching all projects with createdBy: {}", userId);
        return projectRepository.findByCreatedBy_IdAndStatusNot(userId, ProjectStatus.CANCELLED).stream()
                .map(ProjectMapper::toDTO)
                .toList();
    }

    @Override
    public Page<UserSummaryDTO> getAllUsersByProject(
            UUID projectId,
            int page, int size,
            String search,
            UserSortField sortBy,
            Sort.Direction direction
    ) {
        LOGGER.info("Finding project {} to retrieve project members", projectId);
        Project project = findProjectById(projectId);
        LOGGER.debug("Project {} loaded successfully", projectId);
        UUID userId = securityUtil.getCurrentUserDetails().getId();
        LOGGER.info("Checking if the user {} is part of project or owns the project {}", userId, projectId);
        authorizationService.validateProjectAccess(project);
        LOGGER.debug("User {} authorized to access members of project {}", userId, projectId);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy.getField()));
        Specification<User> specification = Specification.where(UserSpecification.belongsToProject(projectId))
                .and(UserSpecification.containsSearch(search));
        LOGGER.info("User {} fetching members of project {}", userId, projectId);
        Page<User> users = userRepository.findAll(specification, pageable);
        return users.map(UserMapper::userSummaryDTO);
    }

    @Override
    @Transactional
    public ProjectCreatedResDTO createProject(ProjectRequestDTO requestDTO) {
        UUID userId = securityUtil.getCurrentUserDetails().getId();
        LOGGER.info("User: {} creating project: {}", userId, requestDTO.getName());
        Project project = ProjectMapper.toEntity(requestDTO);
        User user = userRepository.getReferenceById(userId);
        project.setCreatedBy(user);
        projectRepository.save(project);
        LOGGER.info("User {} created a project successfully: {}", userId, project.getId());
        auditService.log(AuditEntityType.PROJECT, project.getId(), AuditAction.CREATE, "Project", null, project.getName(), userId);
        return ProjectMapper.toCreateDTO(project);
    }

    @Override
    @Transactional
    public ProjectResponseDTO updateProject(UUID projectId, ProjectUpdateDTO updateDTO) {
        LOGGER.info("Finding project to update: {}", projectId);
        Project project = findProjectById(projectId);
        LOGGER.debug("Project {} loaded successfully", projectId);
        UUID userId = securityUtil.getCurrentUserDetails().getId();
        if(updateDTO.getName() != null && !project.getName().equals(updateDTO.getName()))  {
            String oldValue = project.getName();
            project.setName(updateDTO.getName());
            auditService.log(AuditEntityType.PROJECT, project.getId(), AuditAction.UPDATE, "Name", oldValue, updateDTO.getName(), userId);
        }
        if(updateDTO.getDescription() != null && !Objects.equals(updateDTO.getDescription(), project.getDescription())) {
            String oldValue = project.getDescription();
            project.setDescription(updateDTO.getDescription());
            auditService.log(AuditEntityType.PROJECT, project.getId(), AuditAction.UPDATE, "Description", oldValue, updateDTO.getDescription(), userId);
        }
        if(updateDTO.getStatus() != null && !project.getStatus().equals(updateDTO.getStatus())) {
            if(updateDTO.getStatus() == ProjectStatus.CANCELLED && project.getStatus() == ProjectStatus.COMPLETED
             || updateDTO.getStatus() == ProjectStatus.COMPLETED && project.getStatus() == ProjectStatus.CANCELLED) {
                LOGGER.warn("Invalid project status transition for project: {} from {} to {}", projectId, project.getStatus(), updateDTO.getStatus());
                throw new InvalidProjectStatusTransition("Invalid project status transition");
            }
            String oldValue = String.valueOf(project.getStatus());
            project.setStatus(updateDTO.getStatus());
            auditService.log(AuditEntityType.PROJECT, project.getId(), AuditAction.UPDATE, "Status", oldValue, updateDTO.getStatus().name(), userId);
        }
        projectRepository.save(project);
        LOGGER.info("Project: {} updated and saved successfully", projectId);
        return ProjectMapper.toDTO(project);
    }

    @Override
    @Transactional
    public String deleteProject(UUID projectId) {
        LOGGER.info("Finding project to delete with id: {}", projectId);
        Project project = findProjectById(projectId);
        LOGGER.debug("Project {} loaded successfully", projectId);
        ProjectStatus oldValue = project.getStatus();
        UUID userId = securityUtil.getCurrentUserDetails().getId();
        if(oldValue == ProjectStatus.CANCELLED) {
            LOGGER.warn("User {} tried to delete already deleted project: {}", userId, projectId);
            throw new ProjectNotFound("Project was already deleted with id: "+ projectId);
        }
        if(oldValue == ProjectStatus.COMPLETED) {
            LOGGER.warn("User {} tried to delete completed project: {}", userId, projectId);
            throw new InvalidProjectStatusTransition("Project was completed and user tried to delete: "+ projectId);
        }
        project.setStatus(ProjectStatus.CANCELLED);
        /**
         * Members are retained because cancelled projects
         * may be reactivated in the future.
            Set<User> projectMembers = project.getMembers();
            for(User user: projectMembers) {
                user.getProjects().remove(project);
            }
            userRepository.saveAll(projectMembers);
            project.setMembers(null); -> creating null pointer exception
            project.getMembers().clear();
        **/

        projectRepository.save(project);
        LOGGER.info("Project marked as cancelled: {}", projectId);
        auditService.log(AuditEntityType.PROJECT, projectId, AuditAction.DELETE, "Status", oldValue.name(), ProjectStatus.CANCELLED.name(), userId);
        return "Project deleted successfully";
    }


    @Override
    @Transactional
    public UserAssignResponseDTO addUserToProject(UUID userId, UUID projectId) {
        LOGGER.info("Finding project to add user: {}", projectId);
        Project project = findProjectById(projectId);
        LOGGER.debug("Found project to add user: {}", projectId);
        authorizationService.validateProjectOwnership(project);
        if(project.getStatus() == ProjectStatus.COMPLETED || project.getStatus() == ProjectStatus.CANCELLED) {
            LOGGER.warn("User cannot be added to an in-active project {}", projectId);
            throw new ProjectInActiveException("User cannot be added to an in-active project {}"+ projectId);
        }
        UUID performedBy = securityUtil.getCurrentUserDetails().getId();
        LOGGER.info("Finding user to add to a project: {}", userId);
        User user = userService.findUserById(userId);
        LOGGER.debug("Found user to add to a project: {}", userId);
        if(!user.isActive()) {
            LOGGER.warn("InActive user {} cannot be part of a project: {}", userId, projectId);
            throw new InActiveUserException("InActive user cannot be part of a project: "+ projectId);
        }
        if(user.getRoles().contains(RoleTypes.ADMIN) || user.getRoles().contains(RoleTypes.MANAGER)) {
            LOGGER.warn("Only developers can be added to a project: {}", projectId);
            throw new InvalidProjectMemberException("Only developers can be added to a project: "+ projectId);
        }
        if(user.getProjects().contains(project)) {
            throw new UserAlreadyPartOfProjectException("User is already part of same project: "+ projectId);
        }
        LOGGER.info("User {} adding a member {} to project: {}", performedBy, userId, projectId);
        user.getProjects().add(project);
        project.getMembers().add(user);
        LOGGER.debug("User {} added a member {} to project: {}", performedBy, userId, projectId);
        auditService.log(AuditEntityType.PROJECT, projectId, AuditAction.UPDATE, "Member added", " - ", user.getName(), performedBy);
        userRepository.save(user);
        return UserMapper.userAssignResponseDTO(user, project);
    }

    @Transactional
    @Override
    public ProjectResponseDTO removeUserFromProject(UUID userId, UUID projectId) {
        LOGGER.info("Finding project to remove user: {}", projectId);
        Project project = findProjectById(projectId);
        LOGGER.debug("Found project to remove user: {}", projectId);
        authorizationService.validateProjectOwnership(project);
        if(project.getStatus() == ProjectStatus.CANCELLED || project.getStatus() == ProjectStatus.COMPLETED) {
            LOGGER.warn("User cannot be removed from an in-active project {}", projectId);
            throw new ProjectInActiveException("User cannot be removed from an in-active project {}"+ projectId);
        }
        UUID performedBy = securityUtil.getCurrentUserDetails().getId();
        LOGGER.info("Finding user to remove from a project: {}", userId);
        User user = userService.findUserById(userId);
        LOGGER.debug("Found user to remove from a project: {}", userId);
        if(user.getRoles().contains(RoleTypes.ADMIN) || user.getRoles().contains(RoleTypes.MANAGER)) {
            LOGGER.warn("Only developers can be removed from a project: {}", projectId);
            throw new InvalidProjectMemberException("Only developers can be removed from a project: "+ projectId);
        }
        if(!user.getProjects().contains(project)) {
            LOGGER.warn("User {} is not part of the project {} to remove from", userId, projectId);
            throw new UserDoesNotBelongToSameProject("User " + userId + " is not assigned to project " + projectId);
        }
        LOGGER.info("User {} trying to remove a member {} from project {}", performedBy, userId, projectId);
        user.getProjects().remove(project);
        project.getMembers().remove(user);
        LOGGER.info("User {} removed a member {} from project {}", performedBy, userId, projectId);
        List<Task> userProjectTasks = taskRepository.findByAssignedTo_IdAndProject_Id(userId, projectId);
        userProjectTasks.forEach(task -> task.setAssignedTo(null));
        /**
         * Getting all tasks and then filtering again slows down the performance
         * List<Task> userTasks = user.getTasks();
        for(Task task: userTasks) {
            if(task.getProject().equals(project)) {
                task.setAssignedTo(null);
            }
        } **/
        taskRepository.saveAll(userProjectTasks);
        userRepository.save(user);
        projectRepository.save(project);
        auditService.log(AuditEntityType.PROJECT, projectId, AuditAction.DELETE, "Member removed", user.getName(), " - ", performedBy);
        return ProjectMapper.toDTO(project);
    }

}
