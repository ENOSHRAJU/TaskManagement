package com.example.taskManagement.Service;

import com.example.taskManagement.Configurations.AuthorizationService;
import com.example.taskManagement.Configurations.CustomUserDetails;
import com.example.taskManagement.Configurations.SecurityUtil;
import com.example.taskManagement.DTOs.TaskRequestDTO;
import com.example.taskManagement.DTOs.TaskResponseDTO;
import com.example.taskManagement.DTOs.TaskStatusUpdateDTO;
import com.example.taskManagement.DTOs.TaskUpdateDTO;
import com.example.taskManagement.Enums.*;
import com.example.taskManagement.Exception.*;
import com.example.taskManagement.Model.Project;
import com.example.taskManagement.Model.Task;
import com.example.taskManagement.Model.User;
import com.example.taskManagement.Repository.ProjectRepository;
import com.example.taskManagement.Repository.TaskRepository;
import com.example.taskManagement.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private AuditService auditService;

    @Mock
    private SecurityUtil securityUtil;

    @InjectMocks
    private TaskServiceImpl taskServiceImpl;

    private Task task;
    private Project project;
    private User user;

    @BeforeEach
    void setUp() {
        UUID taskId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        project = new Project();
        project.setId(projectId);
        project.setName("Test project");
        project.setDescription("Testing project description");

        task = new Task();
        task.setId(taskId);
        task.setTitle("Test task");
        task.setDescription("Task description for testing");

        user = new User();
        user.setId(UUID.randomUUID());
    }

    @Test
    public void getAllTasks_returnsMappedPage_whenTasksExists() {
        task.setProject(project);
        when(securityUtil.getCurrentUserDetails()).thenReturn(new CustomUserDetails(user));
        when(securityUtil.isAdmin()).thenReturn(false);
        when(securityUtil.isManager()).thenReturn(false);

        Page<Task> pageable = new PageImpl<>(List.of(task));
        when(taskRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(pageable);
        Page<TaskResponseDTO> result = taskServiceImpl.getAllTasks(
                0, 10, null, null, null, TaskSortField.CREATED_AT, Sort.Direction.DESC,
                null, null, null
        );
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(task.getId());
    }

    @Test
    public void getAllTasks_returnsEmptyPage_whenNoTasksMatch() {
        when(securityUtil.getCurrentUserDetails()).thenReturn(new CustomUserDetails(user));
        when(securityUtil.isManager()).thenReturn(false);
        when(securityUtil.isAdmin()).thenReturn(false);
        Page<Task> pageable = new PageImpl<>(List.of());
        when(taskRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(pageable);

        Page<TaskResponseDTO> result = taskServiceImpl.getAllTasks(
                0, 10, null, null, null, TaskSortField.CREATED_AT, Sort.Direction.DESC,
                null, null, null
        );

        assertThat(result.getContent()).hasSize(0);
    }


    @Test
    public void getTaskById_returnsTask_whenTaskExists() {
        task.setStatus(TaskStatus.OPEN);
        task.setTaskPriority(TaskPriority.PRIORITY_5);
        task.setTaskCategory(TaskCategory.QA);
        task.setCreatedAt(LocalDateTime.now());
        task.setProject(project);
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        TaskResponseDTO result = taskServiceImpl.getTaskById(task.getId());

        assertThat(result.getId()).isEqualTo(task.getId());
        assertThat(result.getDescription()).isEqualTo(task.getDescription());
        assertThat(result.getStatus()).isEqualTo(task.getStatus());
        assertThat(result.getTaskPriority()).isEqualTo(task.getTaskPriority());
        assertThat(result.getTaskCategory()).isEqualTo(task.getTaskCategory());
        assertThat(result.getProjectId()).isEqualTo(task.getProject().getId());
    }

    @Test
    public void getTaskById_throwsTaskNotFound_whenTaskDoesNotExist() {
        when(taskRepository.findById(task.getId())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> taskServiceImpl.getTaskById(task.getId()))
                .isInstanceOf(TaskNotFound.class)
                .hasMessageContaining(task.getId().toString());
    }

    @Test
    public void getTaskById_throwsTaskNotFound_whenTaskDeleted() {
        task.setDeleted(true);
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        assertThatThrownBy(() -> taskServiceImpl.getTaskById(task.getId()))
                .isInstanceOf(TaskNotFound.class)
                .hasMessageContaining(task.getId().toString());
    }

    @Test
    public void getTaskById_throwsAccessDenied_whenUnAuthorized() {
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        doThrow(new AccessDeniedException("You are not authorized to access this project"))
                .when(authorizationService).validateProjectAccess(task.getProject());

        assertThatThrownBy(() -> taskServiceImpl.getTaskById(task.getId()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("not authorized to access this project");
    }

    @Test
    public void getTasksByProject_returnsMappedPage_whenTasksExists() {
        task.setProject(project);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(securityUtil.getCurrentUserDetails()).thenReturn(new CustomUserDetails(user));
        when(securityUtil.isAdmin()).thenReturn(false);
        when(securityUtil.isManager()).thenReturn(false);
        Page<Task> pageable = new PageImpl<>(List.of(task));
        when(taskRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(pageable);

        Page<TaskResponseDTO> result = taskServiceImpl.getTasksByProject(
                project.getId(), 0, 10, null, null, null, null, null, TaskSortField.CREATED_AT, Sort.Direction.DESC
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(task.getId());
    }

    @Test
    public void getTasksByProject_returnEmptyPage_whenNoTasksExists() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(securityUtil.getCurrentUserDetails()).thenReturn(new CustomUserDetails(user));
        when(securityUtil.isAdmin()).thenReturn(false);
        when(securityUtil.isManager()).thenReturn(false);
        Page<Task> pageable = new PageImpl<>(List.of());
        when(taskRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(pageable);

        Page<TaskResponseDTO> result = taskServiceImpl.getTasksByProject(
                project.getId(), 0, 10, null, null, null, null, null, TaskSortField.CREATED_AT, Sort.Direction.DESC
        );

        assertThat(result.getContent()).hasSize(0);
    }

    @Test
    public void getTasksByProject_throwsProjectNotFound_whenNoTasksExists() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> taskServiceImpl.getTasksByProject(
                project.getId(), 0, 10, null, null, null, null, null, TaskSortField.CREATED_AT, Sort.Direction.DESC
        )).isInstanceOf(ProjectNotFound.class)
                .hasMessageContaining(project.getId().toString());
    }

    @Test
    public void getTasksByProject_throwsAccessDenied_whenNotAuthorized() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        doThrow(new AccessDeniedException("You are not authorized to access this project"))
                .when(authorizationService).validateProjectAccess(project);

        assertThatThrownBy(() -> taskServiceImpl.getTasksByProject(project.getId(), 0, 10, null, null, null, null, null, TaskSortField.CREATED_AT, Sort.Direction.DESC))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("not authorized");
    }

    @Test
    public void createTask_createsTask_whenAuthorized() {
        TaskRequestDTO requestDTO = new TaskRequestDTO();
        requestDTO.setTitle("Task creation test");
        requestDTO.setDescription("Testing whether task creation is working or not");
        requestDTO.setTaskCategory(TaskCategory.BACK_END);
        requestDTO.setTaskPriority(TaskPriority.PRIORITY_4);
        requestDTO.setDueDate(LocalDate.now().plusWeeks(2));

        project.setStatus(ProjectStatus.ACTIVE);

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(securityUtil.getCurrentUserDetails()).thenReturn(new CustomUserDetails(user));

        TaskResponseDTO result = taskServiceImpl.createTask(project.getId(), requestDTO);

        assertThat(result.getTitle()).isEqualTo(requestDTO.getTitle());
        assertThat(result.getDescription()).isEqualTo(requestDTO.getDescription());
        assertThat(result.getTaskCategory()).isEqualTo(requestDTO.getTaskCategory());
        assertThat(result.getTaskPriority()).isEqualTo(requestDTO.getTaskPriority());
        verify(taskRepository).save(any(Task.class));
        verify(auditService).log(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    public void createTask_throwsProjectNotFound_whenNoProject() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> taskServiceImpl.createTask(project.getId(), null))
                .isInstanceOf(ProjectNotFound.class)
                .hasMessageContaining(project.getId().toString());
        verify(taskRepository, never()).save(any());
        verify(auditService, never()).log(any(),any(),any(),any(),any(),any(),any());
    }

    @Test
    public void createTask_throwsProjectInActiveException_whenProjectIsComplete() {
        project.setStatus(ProjectStatus.COMPLETED);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        assertThatThrownBy(() -> taskServiceImpl.createTask(project.getId(), null))
                .isInstanceOf(ProjectInActiveException.class)
                .hasMessageContaining(project.getId().toString());
        verify(taskRepository, never()).save(any());
        verify(auditService, never()).log(any(),any(),any(),any(),any(),any(),any());
    }

    @Test
    public void createTask_throwsProjectInActiveException_whenProjectIsCancelled() {
        project.setStatus(ProjectStatus.CANCELLED);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        assertThatThrownBy(() -> taskServiceImpl.createTask(project.getId(), null))
                .isInstanceOf(ProjectInActiveException.class)
                .hasMessageContaining(project.getId().toString());
        verify(taskRepository, never()).save(any());
        verify(auditService, never()).log(any(),any(),any(),any(),any(),any(),any());
    }

    @Test
    public void createTask_throwsAccessDenied_whenUnAuthorised() {
        project.setStatus(ProjectStatus.ACTIVE);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        doThrow(new AccessDeniedException("You are not authorized to access this project"))
                .when(authorizationService).validateProjectOwnership(project);
        assertThatThrownBy(() -> taskServiceImpl.createTask(project.getId(), null))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("not authorized");
        verify(taskRepository, never()).save(any());
        verify(auditService, never()).log(any(),any(),any(),any(),any(),any(),any());
    }

    @Test
    public void updateTask_returnsUpdatedTask_whenTaskExists() {
        project.setStatus(ProjectStatus.ACTIVE);
        task.setProject(project);
        task.setDeleted(false);
        task.setTaskPriority(TaskPriority.PRIORITY_2);

        TaskUpdateDTO updateDTO = new TaskUpdateDTO();
        updateDTO.setTitle("Update Task");
        updateDTO.setDescription("Testing updating task");
        updateDTO.setTaskPriority(TaskPriority.PRIORITY_4);
        updateDTO.setDueDate(LocalDate.now().plusWeeks(2));

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(securityUtil.getCurrentUserDetails()).thenReturn(new CustomUserDetails(user));

        TaskResponseDTO result = taskServiceImpl.updateTask(project.getId(), task.getId(), updateDTO);
        verify(auditService, times(4)).log(any(),any(),any(),any(),any(),any(),any());
        verify(taskRepository).save(any(Task.class));
        assertThat(result.getTitle()).isEqualTo(updateDTO.getTitle());
        assertThat(result.getDescription()).isEqualTo(updateDTO.getDescription());
        assertThat(result.getTaskPriority()).isEqualTo(updateDTO.getTaskPriority());
        assertThat(result.getDueDate()).isEqualTo(updateDTO.getDueDate());
    }

    @Test
    public void updateTask_updatesTaskTitle_whenOnlyTaskTitleProvided() {
        project.setStatus(ProjectStatus.ACTIVE);
        task.setProject(project);
        task.setDeleted(false);

        TaskUpdateDTO updateDTO = new TaskUpdateDTO();
        updateDTO.setTitle("Update task title");

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(securityUtil.getCurrentUserDetails()).thenReturn(new CustomUserDetails(user));

        TaskResponseDTO result = taskServiceImpl.updateTask(project.getId(), task.getId(), updateDTO);
        verify(auditService).log(any(),any(),any(),any(),any(),any(),any());
        verify(taskRepository).save(any(Task.class));
        assertThat(result.getTitle()).isEqualTo(updateDTO.getTitle());
    }

    @Test
    public void updateTask_updatesTaskDescription_whenOnlyTaskDescriptionProvided() {
        project.setStatus(ProjectStatus.ACTIVE);
        task.setProject(project);
        task.setDeleted(false);

        TaskUpdateDTO updateDTO = new TaskUpdateDTO();
        updateDTO.setDescription("Updates task description");

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(securityUtil.getCurrentUserDetails()).thenReturn(new CustomUserDetails(user));

        TaskResponseDTO result = taskServiceImpl.updateTask(project.getId(), task.getId(), updateDTO);
        verify(auditService).log(any(),any(),any(),any(),any(),any(),any());
        verify(taskRepository).save(any(Task.class));
        assertThat(result.getDescription()).isEqualTo(updateDTO.getDescription());
    }

    @Test
    public void updateTask_updatesTaskPriority_whenOnlyTaskPriorityProvided() {
        project.setStatus(ProjectStatus.ACTIVE);
        task.setProject(project);
        task.setDeleted(false);
        task.setTaskPriority(TaskPriority.PRIORITY_2);

        TaskUpdateDTO updateDTO = new TaskUpdateDTO();
        updateDTO.setTaskPriority(TaskPriority.PRIORITY_4);

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(securityUtil.getCurrentUserDetails()).thenReturn(new CustomUserDetails(user));

        TaskResponseDTO result = taskServiceImpl.updateTask(project.getId(), task.getId(), updateDTO);
        verify(auditService).log(any(),any(),any(),any(),any(),any(),any());
        verify(taskRepository).save(any(Task.class));
        assertThat(result.getTaskPriority()).isEqualTo(updateDTO.getTaskPriority());
    }

    @Test
    public void updateTask_updatesDueDate_whenOnlyTaskDueDateProvided() {
        project.setStatus(ProjectStatus.ACTIVE);
        task.setProject(project);
        task.setDeleted(false);

        TaskUpdateDTO updateDTO = new TaskUpdateDTO();
        updateDTO.setDueDate(LocalDate.now().plusWeeks(2));

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(securityUtil.getCurrentUserDetails()).thenReturn(new CustomUserDetails(user));

        TaskResponseDTO result = taskServiceImpl.updateTask(project.getId(), task.getId(), updateDTO);
        verify(auditService).log(any(),any(),any(),any(),any(),any(),any());
        verify(taskRepository).save(any(Task.class));
        assertThat(result.getDueDate()).isEqualTo(updateDTO.getDueDate());
    }

    @Test
    public void updateTask_throwsException_whenProjectIsCompleted() {
        project.setStatus(ProjectStatus.COMPLETED);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        assertThatThrownBy(() -> taskServiceImpl.updateTask(project.getId(), task.getId(), null))
                .isInstanceOf(ProjectInActiveException.class)
                .hasMessageContaining(project.getId().toString());
        verify(taskRepository, never()).save(any());
        verify(auditService, never()).log(any(),any(),any(),any(),any(),any(),any());
    }

    @Test
    public void updateTask_throwsException_whenProjectIsCancelled() {
        project.setStatus(ProjectStatus.CANCELLED);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        assertThatThrownBy(() -> taskServiceImpl.updateTask(project.getId(), task.getId(), null))
                .isInstanceOf(ProjectInActiveException.class)
                .hasMessageContaining(project.getId().toString());
        verify(taskRepository, never()).save(any());
        verify(auditService, never()).log(any(),any(),any(),any(),any(),any(),any());
    }

    @Test
    public void updateTask_throwsException_whenUnAuthorized() {
        project.setStatus(ProjectStatus.ACTIVE);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        doThrow(new AccessDeniedException("You are not authorized to access this project"))
                .when(authorizationService).validateProjectOwnership(project);
        assertThatThrownBy(() -> taskServiceImpl.updateTask(project.getId(), task.getId(), null))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("not authorized");
        verify(taskRepository, never()).save(any());
        verify(auditService, never()).log(any(),any(),any(),any(),any(),any(),any());
    }

    @Test
    public void updateTask_throwsException_whenTaskDoesNotBelongToProject() {
        Project newProject = new Project();
        newProject.setId(UUID.randomUUID());
        project.setStatus(ProjectStatus.ACTIVE);
        task.setProject(newProject);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        assertThatThrownBy(() -> taskServiceImpl.updateTask(project.getId(), task.getId(), null))
                .isInstanceOf(TaskDoesNotBelongToSameProject.class)
                .hasMessageContaining("Task does not belong to same project");
        verify(taskRepository, never()).save(any());
        verify(auditService, never()).log(any(),any(),any(),any(),any(),any(),any());
    }

    @Test
    public void updateTask_throwsException_whenTaskIsDeleted() {
        project.setStatus(ProjectStatus.ACTIVE);
        task.setDeleted(true);
        task.setProject(project);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        assertThatThrownBy(() -> taskServiceImpl.updateTask(project.getId(), task.getId(), null))
                .isInstanceOf(TaskNotFound.class)
                .hasMessageContaining(task.getId().toString());
        verify(taskRepository, never()).save(any());
        verify(auditService, never()).log(any(),any(),any(),any(),any(),any(),any());
    }

    @Test
    public void updateTask_throwsException_whenUpdateRequestHaveEmptyTitle() {
        project.setStatus(ProjectStatus.ACTIVE);
        task.setDeleted(false);
        task.setProject(project);

        TaskUpdateDTO updateDTO = new TaskUpdateDTO();
        updateDTO.setTitle(" ");

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        assertThatThrownBy(() -> taskServiceImpl.updateTask(project.getId(), task.getId(), updateDTO))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("title cannot be blank");
        verify(taskRepository, never()).save(any());
        verify(auditService, never()).log(any(),any(),any(),any(),any(),any(),any());
    }

    @Test
    public void updateTask_throwsException_whenUpdateRequestHaveEmptyDescription() {
        project.setStatus(ProjectStatus.ACTIVE);
        task.setDeleted(false);
        task.setProject(project);

        TaskUpdateDTO updateDTO = new TaskUpdateDTO();
        updateDTO.setTitle("Update task request");
        updateDTO.setDescription(" ");

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        assertThatThrownBy(() -> taskServiceImpl.updateTask(project.getId(), task.getId(), updateDTO))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("description cannot be blank");
        verify(taskRepository, never()).save(any());
        verify(auditService, never()).log(any(),any(),any(),any(),any(),any(),any());
    }

    @Test
    public void assignUser_assignsUser_whenAuthorized() {
        project.setStatus(ProjectStatus.ACTIVE);
        task.setProject(project);
        task.setDeleted(false);
        task.setAssignedTo(null);
        task.setStatus(TaskStatus.OPEN);
        user.getProjects().add(project);

        User performedBy = new User();
        performedBy.setName("Admin");

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(userRepository.findByIdAndActiveTrue(user.getId())).thenReturn(Optional.of(user));
        when(securityUtil.getCurrentUserDetails()).thenReturn(new CustomUserDetails(performedBy));

        TaskResponseDTO result = taskServiceImpl.assignUser(project.getId(), user.getId(), task.getId());
        assertThat(result.getAssignedTo().getId()).isEqualTo(user.getId());
        assertThat(result.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        verify(taskRepository).save(any(Task.class));
        verify(auditService).log(any(),any(),any(),any(),any(),any(),any());
    }

    @Test
    public void assignUser_throwsException_whenProjectNotFound() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> taskServiceImpl.assignUser(project.getId(), user.getId(), task.getId()))
                .isInstanceOf(ProjectNotFound.class)
                .hasMessageContaining(project.getId().toString());
        verify(taskRepository, never()).save(any());
        verify(auditService, never()).log(any(),any(),any(),any(),any(),any(),any());
    }

    @Test
    public void assignUser_throwsException_whenProjectIsComplete() {
        project.setStatus(ProjectStatus.COMPLETED);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        assertThatThrownBy(() -> taskServiceImpl.assignUser(project.getId(), user.getId(), task.getId()))
                .isInstanceOf(ProjectInActiveException.class)
                .hasMessageContaining(project.getId().toString());
        verify(taskRepository, never()).save(any());
        verify(auditService, never()).log(any(),any(),any(),any(),any(),any(),any());
    }

    @Test
    public void assignUser_throwsException_whenProjectIsCancelled() {
        project.setStatus(ProjectStatus.CANCELLED);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        assertThatThrownBy(() -> taskServiceImpl.assignUser(project.getId(), user.getId(), task.getId()))
                .isInstanceOf(ProjectInActiveException.class)
                .hasMessageContaining(project.getId().toString());
        verify(taskRepository, never()).save(any());
        verify(auditService, never()).log(any(),any(),any(),any(),any(),any(),any());
    }

    @Test
    public void assignUser_throwException_whenNotAuthorized() {
        project.setStatus(ProjectStatus.ACTIVE);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        doThrow(new AccessDeniedException("You are not authorized to access this project"))
                .when(authorizationService).validateProjectAccess(project);
        assertThatThrownBy(() -> taskServiceImpl.assignUser(project.getId(), user.getId(), task.getId()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("not authorized");
        verify(taskRepository, never()).save(any());
        verify(auditService, never()).log(any(),any(),any(),any(),any(),any(),any());
    }

    @Test
    public void assignUser_throwsException_whenTaskNotFound() {
        project.setStatus(ProjectStatus.ACTIVE);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(taskRepository.findById(task.getId())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> taskServiceImpl.assignUser(project.getId(), user.getId(), task.getId()))
                .isInstanceOf(TaskNotFound.class)
                .hasMessageContaining(task.getId().toString());
        verify(taskRepository, never()).save(any());
        verify(auditService, never()).log(any(),any(),any(),any(),any(),any(),any());

    }

    @Test
    public void assignUser_throwsException_whenTaskDoesNotBelongToSameProject() {
        project.setStatus(ProjectStatus.ACTIVE);
        Project newProject = new Project();
        newProject.setId(UUID.randomUUID());
        newProject.setName("Test project");
        task.setProject(newProject);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        assertThatThrownBy(() -> taskServiceImpl.assignUser(project.getId(), user.getId(), task.getId()))
                .isInstanceOf(TaskDoesNotBelongToSameProject.class)
                .hasMessageContaining("does not belong to this project");
        verify(taskRepository, never()).save(any());
        verify(auditService, never()).log(any(),any(),any(),any(),any(),any(),any());
    }

    @Test
    public void assignUser_throwsException_whenTaskIsDeleted() {
        project.setStatus(ProjectStatus.ACTIVE);
        task.setProject(project);
        task.setDeleted(true);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        assertThatThrownBy(() -> taskServiceImpl.assignUser(project.getId(), user.getId(), task.getId()))
                .isInstanceOf(TaskNotFound.class)
                .hasMessageContaining(task.getId().toString());
        verify(taskRepository, never()).save(any());
        verify(auditService, never()).log(any(),any(),any(),any(),any(),any(),any());

    }

    @Test
    public void assignUser_throwsException_whenUserNotFound() {
        project.setStatus(ProjectStatus.ACTIVE);
        task.setProject(project);
        task.setDeleted(false);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(userRepository.findByIdAndActiveTrue(user.getId())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> taskServiceImpl.assignUser(project.getId(), user.getId(), task.getId()))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining(user.getId().toString());
        verify(taskRepository, never()).save(any());
        verify(auditService, never()).log(any(),any(),any(),any(),any(),any(),any());
    }

    @Test
    public void assignUser_throwsException_whenUserDoesNotBelongToSameProject() {
        project.setStatus(ProjectStatus.ACTIVE);
        task.setProject(project);
        task.setDeleted(false);
        user.setProjects(new HashSet<>());
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(userRepository.findByIdAndActiveTrue(user.getId())).thenReturn(Optional.of(user));
        assertThatThrownBy(() -> taskServiceImpl.assignUser(project.getId(), user.getId(), task.getId()))
                .isInstanceOf(UserDoesNotBelongToSameProject.class)
                .hasMessageContaining(user.getId().toString());
        verify(taskRepository, never()).save(any());
        verify(auditService, never()).log(any(),any(),any(),any(),any(),any(),any());
    }

    @Test
    public void assignUser_throwsException_whenUserIsAlreadyAssignedToSameTask() {
        project.setStatus(ProjectStatus.ACTIVE);
        task.setProject(project);
        task.setDeleted(false);
        task.setAssignedTo(user);
        user.getProjects().add(project);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(userRepository.findByIdAndActiveTrue(user.getId())).thenReturn(Optional.of(user));
        assertThatThrownBy(() -> taskServiceImpl.assignUser(project.getId(), user.getId(), task.getId()))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining(user.getId().toString());
        verify(taskRepository, never()).save(any());
        verify(auditService, never()).log(any(),any(),any(),any(),any(),any(),any());
    }

    @Test
    public void updateTaskStatus_updatesTaskStatus_whenAuthorized() {
        project.setStatus(ProjectStatus.ACTIVE);
        task.setAssignedTo(user);
        task.setProject(project);
        task.setDeleted(false);
        task.setStatus(TaskStatus.IN_PROGRESS);

        TaskStatusUpdateDTO updateDTO = new TaskStatusUpdateDTO();
        updateDTO.setStatus(TaskStatus.ONHOLD);

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(securityUtil.getCurrentUserDetails()).thenReturn(new CustomUserDetails(user));

        TaskResponseDTO result = taskServiceImpl.updateTaskStatus(project.getId(), task.getId(), updateDTO);
        assertThat(result.getStatus()).isEqualTo(updateDTO.getStatus());
        verify(taskRepository).save(any(Task.class));
        verify(auditService).log(any(),any(),any(),any(),any(),any(),any());
    }

    @Test
    public void updateTaskStatus_throwsException_whenProjectNotFound() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> taskServiceImpl.updateTaskStatus(project.getId(), task.getId(), null))
                .isInstanceOf(ProjectNotFound.class)
                .hasMessageContaining(project.getId().toString());
        verify(taskRepository, never()).save(any());
        verify(auditService, never()).log(any(),any(),any(),any(),any(),any(),any());
    }

    @Test
    public void updateTaskStatus_throwsException_whenProjectIsCancelled() {
        project.setStatus(ProjectStatus.CANCELLED);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        assertThatThrownBy(() -> taskServiceImpl.updateTaskStatus(project.getId(), task.getId(), null))
                .isInstanceOf(ProjectInActiveException.class)
                .hasMessageContaining(project.getId().toString());
        verify(taskRepository, never()).save(any());
        verify(auditService, never()).log(any(),any(),any(),any(),any(),any(),any());
    }

    @Test
    public void updateTaskStatus_throwsException_whenProjectIsCompleted() {
        project.setStatus(ProjectStatus.COMPLETED);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        assertThatThrownBy(() -> taskServiceImpl.updateTaskStatus(project.getId(), task.getId(), null))
                .isInstanceOf(ProjectInActiveException.class)
                .hasMessageContaining(project.getId().toString());
        verify(taskRepository, never()).save(any());
        verify(auditService, never()).log(any(),any(),any(),any(),any(),any(),any());
    }

    @Test
    public void updateTaskStatus_throwsException_whenNotAuthorized() {
        project.setStatus(ProjectStatus.ACTIVE);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        doThrow(new AccessDeniedException("You are not authorized to access this project"))
                .when(authorizationService).validateProjectAccess(project);
        assertThatThrownBy(() -> taskServiceImpl.updateTaskStatus(project.getId(), task.getId(), null))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("not authorized");
        verify(taskRepository, never()).save(any());
        verify(auditService, never()).log(any(),any(),any(),any(),any(),any(),any());
    }

    @Test
    public void updateTaskStatus_throwsException_whenTaskIsNotAssignedToAnyOne() {
        project.setStatus(ProjectStatus.ACTIVE);
        task.setAssignedTo(null);
        TaskStatusUpdateDTO statusUpdateDTO = new TaskStatusUpdateDTO();
        statusUpdateDTO.setStatus(TaskStatus.ONHOLD);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskServiceImpl.updateTaskStatus(project.getId(), task.getId(), statusUpdateDTO))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("Task must be assigned");
        verify(taskRepository, never()).save(any());
        verify(auditService, never()).log(any(),any(),any(),any(),any(),any(),any());
    }

    @Test
    public void updateTaskStatus_throwsException_whenTaskDoesNotBelongToSameProject() {
        project.setStatus(ProjectStatus.ACTIVE);
        task.setAssignedTo(user);
        Project newProject = new Project();
        newProject.setId(UUID.randomUUID());
        newProject.setName("Test project");
        task.setProject(newProject);
        TaskStatusUpdateDTO statusUpdateDTO = new TaskStatusUpdateDTO();
        statusUpdateDTO.setStatus(TaskStatus.ONHOLD);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskServiceImpl.updateTaskStatus(project.getId(), task.getId(), statusUpdateDTO))
                .isInstanceOf(TaskDoesNotBelongToSameProject.class)
                .hasMessageContaining("does not belong to this project");
        verify(taskRepository, never()).save(any());
        verify(auditService, never()).log(any(),any(),any(),any(),any(),any(),any());
    }

    @Test
    public void updateTaskStatus_throwsException_whenTaskIsDelete() {
        project.setStatus(ProjectStatus.ACTIVE);
        task.setAssignedTo(user);
        task.setProject(project);
        task.setDeleted(true);
        TaskStatusUpdateDTO statusUpdateDTO = new TaskStatusUpdateDTO();
        statusUpdateDTO.setStatus(TaskStatus.ONHOLD);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskServiceImpl.updateTaskStatus(project.getId(), task.getId(), statusUpdateDTO))
                .isInstanceOf(TaskNotFound.class)
                .hasMessageContaining(task.getId().toString());
        verify(taskRepository, never()).save(any());
        verify(auditService, never()).log(any(),any(),any(),any(),any(),any(),any());
    }

    @Test
    public void updateTaskStatus_throwsException_whenStatusUpdateIsSame() {
        project.setStatus(ProjectStatus.ACTIVE);
        task.setAssignedTo(user);
        task.setProject(project);
        task.setStatus(TaskStatus.ONHOLD);
        TaskStatusUpdateDTO statusUpdateDTO = new TaskStatusUpdateDTO();
        statusUpdateDTO.setStatus(TaskStatus.ONHOLD);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskServiceImpl.updateTaskStatus(project.getId(), task.getId(), statusUpdateDTO))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("requested status");
        verify(taskRepository, never()).save(any());
        verify(auditService, never()).log(any(),any(),any(),any(),any(),any(),any());
    }

    @Test
    public void deleteTask_deletesTask_whenAuthorized() {
        project.setStatus(ProjectStatus.ACTIVE);
        task.setDeleted(false);
        task.setProject(project);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(securityUtil.getCurrentUserDetails()).thenReturn(new CustomUserDetails(user));
        assertThat(taskServiceImpl.deleteTask(project.getId(),task.getId()))
                .isEqualTo("Deleted successfully");
        assertThat(task.isDeleted()).isEqualTo(true);
        assertThat(task.getStatus()).isEqualTo(TaskStatus.CANCELLED);
        verify(taskRepository).save(any(Task.class));
        verify(auditService).log(any(),any(),any(),any(),any(),any(),any());
    }

    @Test
    public void deleteTask_throwsException_whenProjectNotFound() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> taskServiceImpl.deleteTask(project.getId(), task.getId()))
                .isInstanceOf(ProjectNotFound.class)
                .hasMessageContaining(project.getId().toString());
        verify(taskRepository, never()).save(any());
        verify(auditService, never()).log(any(),any(),any(),any(),any(),any(),any());
    }

    @Test
    public void deleteTask_throwsException_whenProjectIsInActive() {
        project.setStatus(ProjectStatus.CANCELLED);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        assertThatThrownBy(() -> taskServiceImpl.deleteTask(project.getId(), task.getId()))
                .isInstanceOf(ProjectInActiveException.class)
                .hasMessageContaining(project.getId().toString());
        verify(taskRepository, never()).save(any());
        verify(auditService, never()).log(any(),any(),any(),any(),any(),any(),any());
    }

    @Test
    public void deleteTask_throwsException_whenUnAuthorized() {
        project.setStatus(ProjectStatus.ACTIVE);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        doThrow(new AccessDeniedException("You are not authorized to access this project"))
                .when(authorizationService).validateProjectOwnership(project);
        assertThatThrownBy(() -> taskServiceImpl.deleteTask(project.getId(), task.getId()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("not authorized");
        verify(taskRepository, never()).save(any());
        verify(auditService, never()).log(any(),any(),any(),any(),any(),any(),any());
    }

    @Test
    public void deleteTask_throwsException_whenTaskNotFound() {
        project.setStatus(ProjectStatus.ACTIVE);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(taskRepository.findById(task.getId())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> taskServiceImpl.deleteTask(project.getId(), task.getId()))
                .isInstanceOf(TaskNotFound.class)
                .hasMessageContaining(task.getId().toString());
        verify(taskRepository, never()).save(any());
        verify(auditService, never()).log(any(),any(),any(),any(),any(),any(),any());
    }

    @Test
    public void deleteTask_throwsException_whenTaskDoesNotBelongToSameProject() {
        project.setStatus(ProjectStatus.ACTIVE);
        Project newProject = new Project();
        newProject.setId(UUID.randomUUID());
        task.setProject(newProject);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        assertThatThrownBy(() -> taskServiceImpl.deleteTask(project.getId(), task.getId()))
                .isInstanceOf(TaskDoesNotBelongToSameProject.class)
                .hasMessageContaining("does not belong to this project");
        verify(taskRepository, never()).save(any());
        verify(auditService, never()).log(any(),any(),any(),any(),any(),any(),any());
    }

    @Test
    public void deleteTask_throwsException_whenTaskIsDeleted() {
        project.setStatus(ProjectStatus.ACTIVE);
        task.setProject(project);
        task.setDeleted(true);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        assertThatThrownBy(() -> taskServiceImpl.deleteTask(project.getId(), task.getId()))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("already deleted");
        verify(taskRepository, never()).save(any());
        verify(auditService, never()).log(any(),any(),any(),any(),any(),any(),any());
    }

}
