package com.example.taskManagement.Service;


import com.example.taskManagement.DTOs.TaskRequestDTO;
import com.example.taskManagement.DTOs.TaskResponseDTO;
import com.example.taskManagement.DTOs.TaskStatusUpdateDTO;
import com.example.taskManagement.DTOs.TaskUpdateDTO;
import com.example.taskManagement.Exception.ProjectNotFound;
import com.example.taskManagement.Exception.TaskNotFound;
import com.example.taskManagement.Exception.UserDoesNotBelongToSameProject;
import com.example.taskManagement.Exception.UserNotAssignedToProject;
import com.example.taskManagement.Mapper.TaskMapper;
import com.example.taskManagement.Model.Project;
import com.example.taskManagement.Model.Status;
import com.example.taskManagement.Model.Task;
import com.example.taskManagement.Model.User;
import com.example.taskManagement.Repository.ProjectRepository;
import com.example.taskManagement.Repository.TaskRepository;
import com.example.taskManagement.Repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskServiceImpl.class);

    public TaskServiceImpl(TaskRepository taskRepository, ProjectRepository projectRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    private Task findTaskById(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    LOGGER.warn("Unable to find task with id: {}", taskId);
                    return new TaskNotFound("Unable to find task with id: " + taskId);
                });
    }

    private Project findProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> {
                    LOGGER.warn("Project not found: {}", projectId);
                    return new ProjectNotFound("Project not found: "+ projectId);
                });
    }

    private User findUserById(Long userId) {
        return userRepository.findByIdAndActiveTrue(userId)
                .orElseThrow(() -> {
                    LOGGER.warn("User not found: {}", userId);
                    return new UsernameNotFoundException("User not found: "+ userId);
                });
    }

    @Override
    public List<TaskResponseDTO> getAllTasks() {
        LOGGER.info("Fetching all the tasks (deleted = false)");
        return taskRepository.findByDeletedFalse().stream()
                .map(TaskMapper::toDTO)
                .toList();
    }

    @Override
    public TaskResponseDTO getTaskById(Long taskId) {
        Task task = findTaskById(taskId);
        if (task.isDeleted()) {
            LOGGER.warn("Attempt to access soft deleted task with id: {}", taskId);
            throw new TaskNotFound("Unable to find task with id: "+ taskId);
        }
        LOGGER.info("Task found with id: {}", taskId);
        return TaskMapper.toDTO(task);
    }

    @Override
    public List<TaskResponseDTO> getTasksByProject(Long projectId) {
        Project project = findProjectById(projectId);
        LOGGER.info("Fetching all tasks of project: {}", projectId);
        return taskRepository.findByProjectIdAndDeletedFalse(projectId).stream()
                .map(TaskMapper::toDTO)
                .toList();
    }

    @Override
    public List<TaskResponseDTO> getTasksByUser(Long userId) {
        User user = findUserById(userId);
        LOGGER.info("Fetching all tasks assigned to userId: {}", userId);
        return taskRepository.findByAssignedToIdAndDeletedFalse(userId).stream()
                .map(TaskMapper::toDTO)
                .toList();
    }

    @Override
    public List<TaskResponseDTO> getTasksByStatus(Status status, Long projectId) {
        Project project = findProjectById(projectId);
        LOGGER.info("Fetching all tasks by status {} of a project: {}", status, projectId);
        return taskRepository.findByStatusAndProjectId(status, projectId).stream()
                .map(TaskMapper::toDTO)
                .toList();
    }

    @Override
    public TaskResponseDTO createTask(Long projectId, TaskRequestDTO requestDTO) {
        Project project = findProjectById(projectId);
        Task task = TaskMapper.toEntity(requestDTO, project);
        taskRepository.save(task);
        LOGGER.info("Task created successfully with id: {} in project: {}", task.getId(), projectId);
        return TaskMapper.toDTO(task);
    }

    @Override
    public TaskResponseDTO updateTask(Long taskId, TaskUpdateDTO updateDTO) {
        Task task = findTaskById(taskId);
        if(task.isDeleted()) {
            LOGGER.warn("Attempt to access deleted task with id: {}", taskId);
            throw new TaskNotFound("Attempt to access deleted task with id: " + taskId);
        }
        if(updateDTO.getTitle() != null) task.setTitle(updateDTO.getTitle());
        if(updateDTO.getDescription() != null) task.setDescription(updateDTO.getDescription());
        if(updateDTO.getPriority() != null) task.setPriority(updateDTO.getPriority());
        taskRepository.save(task);
        LOGGER.info("Task updated successfully for task id: {}", taskId);
        return TaskMapper.toDTO(task);
    }

    @Override
    public TaskResponseDTO assignUser(Long userId, Long taskId) {
        User user = findUserById(userId);
        Task task = findTaskById(taskId);
        if(user.getProject() == null) throw new UserNotAssignedToProject("User is not assigned to any project for user"+ user.getId());
        if(task.isDeleted()) {
            LOGGER.warn("Attempt to assign user to deleted task with id: {}", taskId);
            throw new TaskNotFound("Attempt to assign user to deleted task with id: " + taskId);
        }
        if(!user.getProject().getId().equals(task.getProject().getId())) {
            LOGGER.warn("User {} does not belong to project {} for task {}", user.getName(), user.getProject().getId(), task.getProject().getId());
            throw new UserDoesNotBelongToSameProject("User does not belong to same project: "+ user.getProject().getId() + "as task does: "+ task.getProject().getId());
        }
        LOGGER.info("Assigning userId: {} to taskId: {}", userId, taskId);
        task.setAssignedTo(user);
        taskRepository.save(task);
        LOGGER.info("Assigned userId {} to taskId {}", user.getId(), task.getId());
        return TaskMapper.toDTO(task);
    }

    @Override
    public TaskResponseDTO updateTaskStatus(Long taskId, TaskStatusUpdateDTO taskStatusUpdateDTO) {
        Task task = findTaskById(taskId);
        if(task.isDeleted()) {
            LOGGER.warn("Attempt to update deleted task with id: {}", taskId);
            throw new TaskNotFound("Attempt to update deleted task with id: " + taskId);
        }
        task.setStatus(taskStatusUpdateDTO.getStatus());
        taskRepository.save(task);
        LOGGER.info("Task status updated successfully for taskId: {}", taskId);
        return TaskMapper.toDTO(task);
    }

    @Override
    public String deleteTask(Long taskId) {
        Task task = findTaskById(taskId);
        task.setDeleted(true);
        taskRepository.save(task);
        LOGGER.info("Task successfully deleted with id: {}", taskId);
        return "Deleted successfully";
    }
}
