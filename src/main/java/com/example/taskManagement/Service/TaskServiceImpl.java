package com.example.taskManagement.Service;

import com.example.taskManagement.Configurations.AuthorizationService;
import com.example.taskManagement.Configurations.SecurityUtil;
import com.example.taskManagement.DTOs.TaskRequestDTO;
import com.example.taskManagement.DTOs.TaskResponseDTO;
import com.example.taskManagement.DTOs.TaskStatusUpdateDTO;
import com.example.taskManagement.DTOs.TaskUpdateDTO;
import com.example.taskManagement.Enums.*;
import com.example.taskManagement.Exception.*;
import com.example.taskManagement.Mapper.TaskMapper;
import com.example.taskManagement.Model.*;
import com.example.taskManagement.Repository.ProjectRepository;
import com.example.taskManagement.Repository.TaskRepository;
import com.example.taskManagement.Repository.UserRepository;
import com.example.taskManagement.Specifications.TaskSpecification;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TaskServiceImpl implements TaskService {

    private final AuthorizationService authorizationService;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;
    private final AuditService auditService;
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskServiceImpl.class);

    public TaskServiceImpl(AuthorizationService authorizationService, TaskRepository taskRepository, ProjectRepository projectRepository, UserRepository userRepository, SecurityUtil securityUtil, AuditService auditService) {
        this.authorizationService = authorizationService;
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.securityUtil = securityUtil;
        this.auditService = auditService;
    }

    private void validateProjectIsActive(Project project) {
        if (project.getStatus() == ProjectStatus.CANCELLED ||
                project.getStatus() == ProjectStatus.COMPLETED) {
            LOGGER.warn("Attempt to modify inactive project {} with status {}", project.getId(), project.getStatus());
            throw new ProjectInActiveException("Cannot modify an inactive project: " + project.getId());
        }
    }

    private Task findTaskById(UUID taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    LOGGER.warn("Unable to find task with id: {}", taskId);
                    return new TaskNotFound("Unable to find task with id: " + taskId);
                });
    }

    private Project findProjectById(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> {
                    LOGGER.warn("Project not found: {}", projectId);
                    return new ProjectNotFound("Project not found: " + projectId);
                });
    }

    private User findUserById(UUID userId) {
        return userRepository.findByIdAndActiveTrue(userId)
                .orElseThrow(() -> {
                    LOGGER.warn("User not found: {}", userId);
                    return new UsernameNotFoundException("User not found: " + userId);
                });
    }

    private void validateUpdateRequest(TaskUpdateDTO dto) {
        if (dto.getTitle() != null && dto.getTitle().isBlank()) {
            throw new InvalidRequestException("Task title cannot be blank.");
        }
        if (dto.getDescription() != null && dto.getDescription().isBlank()) {
            throw new InvalidRequestException("Task description cannot be blank.");
        }
    }

    @Override
    public Page<TaskResponseDTO> getAllTasks(
            int page, int size,
            TaskStatus status,
            TaskCategory category,
            TaskPriority priority,
            TaskSortField sortBy,
            Sort.Direction direction,
            UUID assignedTo,
            String search,
            UUID projectId
    ) {
        if (projectId != null) {
            LOGGER.info("Validating project existence for projectId={}", projectId);
            findProjectById(projectId);
        }
        UUID userId = securityUtil.getCurrentUserDetails().getId();
        boolean isAdmin = securityUtil.isAdmin();
        boolean isManager = securityUtil.isManager();
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy.getField()));
        Specification<Task> specification = Specification
                .where(TaskSpecification.accessibleTasks(isAdmin, isManager, userId))
                .and(TaskSpecification.belongsToProject(projectId))
                .and(TaskSpecification.hasStatus(status))
                .and(TaskSpecification.hasCategory(category))
                .and(TaskSpecification.hasPriority(priority))
                .and(TaskSpecification.assignedTo(assignedTo))
                .and(TaskSpecification.containsSearch(search));
        LOGGER.info(
                "Fetching tasks | page={}, size={}, status={}, priority={}, category={}, assignedTo={}, projectId={}, search={}, sortBy={}, direction={}",
                page, size, status, priority, category, assignedTo, projectId, search, sortBy, direction);
        Page<Task> tasks = taskRepository.findAll(specification, pageable);
        LOGGER.debug("Retrieved {} task(s)", tasks.getNumberOfElements());
        return tasks.map(TaskMapper::toDTO);
    }

    @Override
    public TaskResponseDTO getTaskById(UUID taskId) {
        LOGGER.info("Finding task with id: {}", taskId);
        Task task = findTaskById(taskId);
        LOGGER.debug("Task {} loaded successfully", taskId);
        authorizationService.validateProjectAccess(task.getProject());
        if (task.isDeleted()) {
            LOGGER.warn("Attempt to access deleted task with id: {}", taskId);
            throw new TaskNotFound("Task not found with id: " + taskId);
        }
        LOGGER.info("Successfully retrieved task {}", taskId);
        return TaskMapper.toDTO(task);
    }

    @Override
    public Page<TaskResponseDTO> getTasksByProject(
            UUID projectId,
            int page, int size,
            TaskStatus status,
            TaskPriority priority,
            TaskCategory category,
            UUID assignedTo,
            String search,
            TaskSortField sortBy,
            Sort.Direction direction
    ) {
        LOGGER.info("Finding project: {}", projectId);
        Project project = findProjectById(projectId);
        LOGGER.debug("Project {} loaded successfully", projectId);
        authorizationService.validateProjectAccess(project);
        return getAllTasks(page, size, status, category, priority, sortBy, direction, assignedTo, search, projectId);
    }

    @Override
    @Transactional
    public TaskResponseDTO createTask(UUID projectId, TaskRequestDTO requestDTO) {
        LOGGER.info("Finding project with id: {}", projectId);
        Project project = findProjectById(projectId);
        LOGGER.debug("Project {} loaded successfully", projectId);
        LOGGER.info("Validating project {} is active before creating task", projectId);
        validateProjectIsActive(project);
        LOGGER.info("Validating user ownership for project: {}", projectId);
        authorizationService.validateProjectOwnership(project);
        UUID performedBy = securityUtil.getCurrentUserDetails().getId();
        Task task = TaskMapper.toEntity(requestDTO, project);
        taskRepository.save(task);
        LOGGER.info("Task {} created successfully in project {} by user {}", task.getId(), projectId, performedBy);
        auditService.log(AuditEntityType.TASK, task.getId(), AuditAction.CREATE, "Task", " - ", task.getTitle(), performedBy);
        return TaskMapper.toDTO(task);
    }

    @Override
    @Transactional
    public TaskResponseDTO updateTask(UUID projectId, UUID taskId, TaskUpdateDTO updateDTO) {
        LOGGER.info("Finding project with id: {}", projectId);
        Project project = findProjectById(projectId);
        LOGGER.debug("Project {} loaded successfully", projectId);
        validateProjectIsActive(project);
        LOGGER.info("Validating project ownership before updating task {}", taskId);
        authorizationService.validateProjectOwnership(project);
        LOGGER.info("Fetching task with id {}", taskId);
        Task task = findTaskById(taskId);
        LOGGER.debug("Task {} loaded successfully", taskId);
        if (!task.getProject().getId().equals(projectId)) {
            LOGGER.warn("Task {} does not belong to project {}", taskId, projectId);
            throw new TaskDoesNotBelongToSameProject("Task does not belong to same project");
        }
        if (task.isDeleted()) {
            LOGGER.warn("Attempt to access deleted task with id: {}", taskId);
            throw new TaskNotFound("Task not found with id: " + taskId);
        }
        // If title and description comes as empty - something like '    '.
        validateUpdateRequest(updateDTO);
        UUID performedBy = securityUtil.getCurrentUserDetails().getId();
        if (updateDTO.getTitle() != null) {
            String oldValue = task.getTitle();
            task.setTitle(updateDTO.getTitle());
            LOGGER.info("User {} updated task {} title", performedBy, taskId);
            auditService.log(AuditEntityType.TASK, taskId, AuditAction.UPDATE, "Task Title", oldValue, updateDTO.getTitle(), performedBy);
        }
        if (updateDTO.getDescription() != null) {
            String oldValue = task.getDescription();
            task.setDescription(updateDTO.getDescription());
            LOGGER.info("User {} updated task {} description", performedBy, taskId);
            auditService.log(AuditEntityType.TASK, taskId, AuditAction.UPDATE, "Task Description", oldValue, updateDTO.getDescription(), performedBy);
        }
        if (updateDTO.getTaskPriority() != null) {
            String oldValue = task.getTaskPriority().name();
            task.setTaskPriority(updateDTO.getTaskPriority());
            LOGGER.info("User {} updated task {} priority", performedBy, taskId);
            auditService.log(AuditEntityType.TASK, taskId, AuditAction.UPDATE, "Task Priority", oldValue, updateDTO.getTaskPriority().name(), performedBy);
        }
        if (updateDTO.getDueDate() != null) {
            String oldValue = String.valueOf(task.getDueDate());
            task.setDueDate(updateDTO.getDueDate());
            LOGGER.info("User {} updated task {} due date", performedBy, taskId);
            auditService.log(AuditEntityType.TASK, taskId, AuditAction.UPDATE, "Task Due date", oldValue, String.valueOf(updateDTO.getDueDate()), performedBy);
        }
        taskRepository.save(task);
        LOGGER.info("Task {} updated successfully by user {}", taskId, performedBy);
        return TaskMapper.toDTO(task);
    }

    @Override
    @Transactional
    public TaskResponseDTO assignUser(UUID projectId, UUID userId, UUID taskId) {
        LOGGER.info("Finding project with id: {}", projectId);
        Project project = findProjectById(projectId);
        LOGGER.debug("Project {} loaded successfully", projectId);
        LOGGER.info("Validating project {} is active before assigning user", projectId);
        validateProjectIsActive(project);
        LOGGER.info("Validating project access before assigning user in project: {}", projectId);
        authorizationService.validateProjectAccess(project);
        LOGGER.info("Finding task: {}", taskId);
        Task task = findTaskById(taskId);
        LOGGER.debug("Task {} loaded successfully", taskId);
        if (!task.getProject().getId().equals(projectId)) {
            LOGGER.warn("Task {} does not belong to project {}", taskId, projectId);
            throw new TaskDoesNotBelongToSameProject("Task does not belong to this project");
        }
        if (task.isDeleted()) {
            LOGGER.warn("Attempt to assign user to deleted task with id: {}", taskId);
            throw new TaskNotFound("Task not found: " + taskId);
        }
        LOGGER.info("Finding user {} to assign task {}", userId, taskId);
        User user = findUserById(userId);
        LOGGER.debug("User {} loaded successfully to assign task {}", userId, taskId);
        if (!user.getProjects().contains(project)) {
            LOGGER.warn("User {} is not a member of project {}", userId, projectId);
            throw new UserDoesNotBelongToSameProject(
                    "User " + userId + " is not a member of project " + projectId);
        }
        if (task.getAssignedTo() != null && task.getAssignedTo().getId().equals(userId)) {
            LOGGER.warn("Task {} is already assigned to the same user {}", taskId, userId);
            throw new InvalidRequestException("Task is already assigned to this user " + userId);
        }
        UUID performedBy = securityUtil.getCurrentUserDetails().getId();
        String oldValue = task.getAssignedTo() == null ? " - " : task.getAssignedTo().getName();
        LOGGER.info("User {} assigning task: {} to developer: {}", performedBy, taskId, userId);
        task.setAssignedTo(user);
        LOGGER.debug("User {} assigned task: {} to developer: {}", performedBy, taskId, userId);
        if (task.getStatus() == TaskStatus.OPEN) {
            task.setStatus(TaskStatus.IN_PROGRESS);
        }
        taskRepository.save(task);
        LOGGER.info("Task {} assigned to user {} by {}", taskId, userId, performedBy);
        auditService.log(AuditEntityType.TASK, taskId, AuditAction.ASSIGN, "Assigned-to", oldValue, user.getName(), performedBy);
        return TaskMapper.toDTO(task);
    }

    @Override
    @Transactional
    public TaskResponseDTO updateTaskStatus(UUID projectId, UUID taskId, TaskStatusUpdateDTO taskStatusUpdateDTO) {
        LOGGER.info("Finding project with id: {}", projectId);
        Project project = findProjectById(projectId);
        LOGGER.debug("Project {} loaded successfully", projectId);
        LOGGER.info("Validating project {} is active before updating task status", projectId);
        validateProjectIsActive(project);
        LOGGER.info("Validating project access before updating task status in project: {}", projectId);
        authorizationService.validateProjectAccess(project);
        LOGGER.info("Finding task {} to update status {}", taskId, taskStatusUpdateDTO.getStatus());
        Task task = findTaskById(taskId);
        LOGGER.debug("Task {} loaded successfully", taskId);
        if (task.getAssignedTo() == null) {
            LOGGER.warn("Task {} is not assigned to any user", taskId);
            throw new InvalidRequestException("Task must be assigned before its status can be changed.");
        }
        if (!task.getProject().getId().equals(projectId)) {
            LOGGER.warn("Task {} does not belong to project {}", taskId, projectId);
            throw new TaskDoesNotBelongToSameProject("Task does not belong to this project");
        }
        if (task.isDeleted()) {
            LOGGER.warn("Attempt to update deleted task: {}", taskId);
            throw new TaskNotFound("Task not found: " + taskId);
        }
        if (task.getStatus().equals(taskStatusUpdateDTO.getStatus())) {
            LOGGER.warn("Task {} is already in status {}", taskId, task.getStatus());
            throw new InvalidRequestException("Task is already in the requested status.");
        }
        UUID performedBy = securityUtil.getCurrentUserDetails().getId();
        String oldValue = task.getStatus().name();
        task.setStatus(taskStatusUpdateDTO.getStatus());
        taskRepository.save(task);
        LOGGER.info("Task {} status changed from {} to {} by user {}", taskId, oldValue, taskStatusUpdateDTO.getStatus(), performedBy);
        auditService.log(AuditEntityType.TASK, taskId, AuditAction.STATUS_CHANGE, "Status", oldValue, taskStatusUpdateDTO.getStatus().name(), performedBy);
        return TaskMapper.toDTO(task);
    }

    @Override
    @Transactional
    public String deleteTask(UUID projectId, UUID taskId) {
        LOGGER.info("Finding project with id: {}", projectId);
        Project project = findProjectById(projectId);
        LOGGER.debug("Project {} loaded successfully", projectId);
        LOGGER.info("Validating project {} is active before deleting task", projectId);
        validateProjectIsActive(project);
        LOGGER.info("Validating project ownership before deleting task in project: {}", projectId);
        authorizationService.validateProjectOwnership(project);
        LOGGER.info("Finding task to delete: {}", taskId);
        Task task = findTaskById(taskId);
        LOGGER.debug("Task {} loaded successfully", taskId);
        if (!task.getProject().getId().equals(projectId)) {
            LOGGER.warn("Task {} does not belong to project {}", taskId, projectId);
            throw new TaskDoesNotBelongToSameProject("Task does not belong to this project");
        }
        if (task.isDeleted()) {
            LOGGER.warn("Task {} is already deleted", taskId);
            throw new InvalidRequestException("Task is already deleted.");
        }
        UUID performedBy = securityUtil.getCurrentUserDetails().getId();
        task.setDeleted(true);
        task.setStatus(TaskStatus.CANCELLED);
        taskRepository.save(task);
        LOGGER.info("Task {} successfully soft-deleted by user {}", taskId, performedBy);
        auditService.log(AuditEntityType.TASK, taskId, AuditAction.DELETE, "Deleted", "false", "true", performedBy);
        return "Deleted successfully";
    }

    @Override
    public List<TaskResponseDTO> getMyOverDueTasks() {
        UUID loggedUserId = securityUtil.getCurrentUserDetails().getId();
        LOGGER.info("Fetching overdue tasks for logged in user {}", loggedUserId);
        List<TaskResponseDTO> overdueTasks = taskRepository
                .findByAssignedToIdAndStatusAndDeletedFalse(loggedUserId, TaskStatus.OVERDUE)
                .stream()
                .map(TaskMapper::toDTO)
                .toList();
        LOGGER.debug("Found {} overdue tasks for user {}", overdueTasks.size(), loggedUserId);
        return overdueTasks;
    }

    @Override
    public List<TaskResponseDTO> getOverdueTasksForUser(UUID userId) {
        LOGGER.info("Fetching overdue tasks for userId: {}", userId);
        findUserById(userId);
        List<TaskResponseDTO> overdueTasks = taskRepository
                .findByAssignedToIdAndStatusAndDeletedFalse(userId, TaskStatus.OVERDUE)
                .stream()
                .map(TaskMapper::toDTO)
                .toList();
        LOGGER.debug("Found {} overdue tasks for userId {}", overdueTasks.size(), userId);
        return overdueTasks;
    }

    @Override
    public List<TaskResponseDTO> getAllOverdueTasks() {
        LOGGER.info("Admin fetching all overdue tasks system-wide");
        List<TaskResponseDTO> overdueTasks = taskRepository
                .findByStatusAndDeletedFalse(TaskStatus.OVERDUE)
                .stream()
                .map(TaskMapper::toDTO)
                .toList();
        LOGGER.debug("Found {} overdue tasks system-wide", overdueTasks.size());
        return overdueTasks;
    }
}