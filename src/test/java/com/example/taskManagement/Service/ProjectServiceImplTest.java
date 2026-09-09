package com.example.taskManagement.Service;

import com.example.taskManagement.Configurations.AuthorizationService;
import com.example.taskManagement.Configurations.CustomUserDetails;
import com.example.taskManagement.Configurations.SecurityUtil;
import com.example.taskManagement.DTOs.*;
import com.example.taskManagement.Enums.*;
import com.example.taskManagement.Exception.*;
import com.example.taskManagement.Model.Project;
import com.example.taskManagement.Model.Task;
import com.example.taskManagement.Model.User;
import com.example.taskManagement.Repository.ProjectRepository;
import com.example.taskManagement.Repository.TaskRepository;
import com.example.taskManagement.Repository.UserRepository;
import org.checkerframework.checker.units.qual.C;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceImplTest {

    private Project project;
    private User user;
    private Task task;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserService userService;

    @Mock
    private SecurityUtil securityUtil;

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private ProjectServiceImpl projectServiceImpl;

    @BeforeEach
    void setUp() {
        project = new Project();
        project.setId(UUID.randomUUID());
        project.setName("Test project");

        user = new User();
        user.setId(UUID.randomUUID());
        user.setName("Test user");

        task = new Task();
        task.setId(UUID.randomUUID());
        task.setTitle("Test task");
    }

    @Test
    public void getAllProjects_returnsPage_WhenProjectsExits() {
        project.setCreatedBy(user);
        Page<Project> pageable = new PageImpl<>(List.of(project));
        when(securityUtil.getCurrentUserDetails()).thenReturn(new CustomUserDetails(user));
        when(projectRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(pageable);

        Page<ProjectSummaryDTO> result = projectServiceImpl.getAllProjects(
                0, 10, null, null, ProjectSortField.CREATED_AT, Sort.Direction.DESC, null
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(project.getId());
    }

    @Test
    public void getAllProjects_returnEmptyPage_whenNoProjectMatch() {
        Page<Project> pageable = new PageImpl<>(List.of());
        when(securityUtil.getCurrentUserDetails()).thenReturn(new CustomUserDetails(user));
        when(projectRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(pageable);
        Page<ProjectSummaryDTO> result = projectServiceImpl.getAllProjects(
                0, 10, null, null, ProjectSortField.CREATED_AT, Sort.Direction.DESC, null
        );

        assertThat(result.getContent()).hasSize(0);
    }

    @Test
    public void getProjectId_returnsProject_whenAuthorized() {
        project.setDescription("Test project description");
        project.setStatus(ProjectStatus.ACTIVE);
        project.setCreatedBy(user);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(securityUtil.getCurrentUserDetails()).thenReturn(new CustomUserDetails(user));
        ProjectResponseDTO result = projectServiceImpl.getProjectById(project.getId());

        assertThat(result.getProjectId()).isEqualTo(project.getId());
        assertThat(result.getName()).isEqualTo(project.getName());
        assertThat(result.getDescription()).isEqualTo(project.getDescription());
        assertThat(result.getStatus()).isEqualTo(project.getStatus());
        assertThat(result.getCreatedBy().getId()).isEqualTo(project.getCreatedBy().getId());
    }

    @Test
    public void getProjectById_throwsProjectNotFound_whenProjectDoesNotExist() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> projectServiceImpl.getProjectById(project.getId()))
                .isInstanceOf(ProjectNotFound.class)
                .hasMessageContaining(project.getId().toString());
    }

    @Test
    public void getProjectById_throwsAccessDenied_whenUnAuthorized() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        doThrow(new AccessDeniedException("You are not authorized to access this project"))
                .when(authorizationService).validateProjectAccess(project);

        assertThatThrownBy(() -> projectServiceImpl.getProjectById(project.getId()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("not authorized");
    }


    @Test
    public void getAllUsersByProject_returnsMappedPage_whenUsersExist() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(securityUtil.getCurrentUserDetails()).thenReturn(new CustomUserDetails(user));
        Page<User> pageable = new PageImpl<>(List.of(user));
        when(userRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(pageable);

        Page<UserSummaryDTO> result = projectServiceImpl.getAllUsersByProject(
                project.getId(), 0, 10, null, UserSortField.NAME, Sort.Direction.DESC
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(user.getId());
    }

    @Test
    public void getAllUsersByProject_throwsProjectNotFound_whenProjectDoesNotExist() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> projectServiceImpl.getAllUsersByProject(
                project.getId(), 0, 10, null, UserSortField.NAME, Sort.Direction.DESC
        )).isInstanceOf(ProjectNotFound.class)
                .hasMessageContaining(project.getId().toString());
    }

    @Test
    public void getAllUsersByProject_throwsAccessDenied_whenUnAuthorized() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(securityUtil.getCurrentUserDetails()).thenReturn(new CustomUserDetails(user));
        doThrow(new AccessDeniedException("You are not authorized to access this project"))
                .when(authorizationService).validateProjectAccess(project);

        assertThatThrownBy(() -> projectServiceImpl.getAllUsersByProject(
                project.getId(), 0, 10, null, UserSortField.NAME, Sort.Direction.DESC
        )).isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("not authorized");
    }

    @Test
    public void createProject_createsProject_whenValid() {
        ProjectRequestDTO requestDTO = new ProjectRequestDTO();
        requestDTO.setName("New project");
        requestDTO.setDescription("New project description");

        when(securityUtil.getCurrentUserDetails()).thenReturn(new CustomUserDetails(user));
        when(userRepository.getReferenceById(user.getId())).thenReturn(user);

        ProjectCreatedResDTO result = projectServiceImpl.createProject(requestDTO);

        assertThat(result.getName()).isEqualTo(requestDTO.getName());
        verify(projectRepository).save(any(Project.class));
        verify(auditService).log(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    public void updateProject_updatesName_whenOnlyNameProvided() {
        project.setStatus(ProjectStatus.ACTIVE);
        project.setName("Old name");
        project.setCreatedBy(user);
        ProjectUpdateDTO updateDTO = new ProjectUpdateDTO();
        updateDTO.setName("Updated name");

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(securityUtil.getCurrentUserDetails()).thenReturn(new CustomUserDetails(user));

        ProjectResponseDTO result = projectServiceImpl.updateProject(project.getId(), updateDTO);

        assertThat(result.getName()).isEqualTo(updateDTO.getName());
        verify(auditService).log(any(), any(), any(), any(), any(), any(), any());
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    public void updateProject_updatesDescription_whenOnlyDescriptionProvided() {
        project.setStatus(ProjectStatus.ACTIVE);
        project.setDescription("Old description");
        project.setCreatedBy(user);
        ProjectUpdateDTO updateDTO = new ProjectUpdateDTO();
        updateDTO.setDescription("Updated description");

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(securityUtil.getCurrentUserDetails()).thenReturn(new CustomUserDetails(user));

        ProjectResponseDTO result = projectServiceImpl.updateProject(project.getId(), updateDTO);

        assertThat(result.getDescription()).isEqualTo(updateDTO.getDescription());
        verify(auditService).log(any(), any(), any(), any(), any(), any(), any());
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    public void updateProject_updatesStatus_whenValidTransition() {
        project.setStatus(ProjectStatus.ACTIVE);
        project.setCreatedBy(user);
        ProjectUpdateDTO updateDTO = new ProjectUpdateDTO();
        updateDTO.setStatus(ProjectStatus.COMPLETED);

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(securityUtil.getCurrentUserDetails()).thenReturn(new CustomUserDetails(user));

        ProjectResponseDTO result = projectServiceImpl.updateProject(project.getId(), updateDTO);

        assertThat(result.getStatus()).isEqualTo(ProjectStatus.COMPLETED);
        verify(auditService).log(any(), any(), any(), any(), any(), any(), any());
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    public void updateProject_doesNotUpdateOrLog_whenValuesAreUnchanged() {
        project.setStatus(ProjectStatus.ACTIVE);
        project.setName("Same name");
        project.setDescription("Same description");
        project.setCreatedBy(user);
        ProjectUpdateDTO updateDTO = new ProjectUpdateDTO();
        updateDTO.setName("Same name");
        updateDTO.setDescription("Same description");
        updateDTO.setStatus(ProjectStatus.ACTIVE);

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(securityUtil.getCurrentUserDetails()).thenReturn(new CustomUserDetails(user));

        projectServiceImpl.updateProject(project.getId(), updateDTO);

        verify(auditService, never()).log(any(), any(), any(), any(), any(), any(), any());
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    public void updateProject_throwsInvalidProjectStatusTransition_whenCompletedToCancelled() {
        project.setStatus(ProjectStatus.COMPLETED);
        ProjectUpdateDTO updateDTO = new ProjectUpdateDTO();
        updateDTO.setStatus(ProjectStatus.CANCELLED);

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(securityUtil.getCurrentUserDetails()).thenReturn(new CustomUserDetails(user));
        assertThatThrownBy(() -> projectServiceImpl.updateProject(project.getId(), updateDTO))
                .isInstanceOf(InvalidProjectStatusTransition.class)
                .hasMessageContaining("Invalid project status transition");
        verify(projectRepository, never()).save(any());
        verify(auditService, never()).log(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    public void updateProject_throwsInvalidProjectStatusTransition_whenCancelledToCompleted() {
        project.setStatus(ProjectStatus.CANCELLED);

        ProjectUpdateDTO updateDTO = new ProjectUpdateDTO();
        updateDTO.setStatus(ProjectStatus.COMPLETED);

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(securityUtil.getCurrentUserDetails()).thenReturn(new CustomUserDetails(user));
        assertThatThrownBy(() -> projectServiceImpl.updateProject(project.getId(), updateDTO))
                .isInstanceOf(InvalidProjectStatusTransition.class)
                .hasMessageContaining("Invalid project status transition");
        verify(projectRepository, never()).save(any());
        verify(auditService, never()).log(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    public void updateProject_throwsProjectNotFound_whenProjectDoesNotExist() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> projectServiceImpl.updateProject(project.getId(), null))
                .isInstanceOf(ProjectNotFound.class)
                .hasMessageContaining(project.getId().toString());
        verify(projectRepository, never()).save(any());
    }

    @Test
    public void updateProject_throwsAccessDenied_whenUnAuthorized() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        doThrow(new AccessDeniedException("You are not authorized to access this project"))
                .when(authorizationService).validateProjectOwnership(project);

        assertThatThrownBy(() -> projectServiceImpl.updateProject(project.getId(), null))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("not authorized");
        verify(projectRepository, never()).save(any());
    }


    @Test
    public void deleteProject_cancelsProject_whenActive() {
        project.setStatus(ProjectStatus.ACTIVE);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(securityUtil.getCurrentUserDetails()).thenReturn(new CustomUserDetails(user));

        String result = projectServiceImpl.deleteProject(project.getId());

        assertThat(result).isEqualTo("Project deleted successfully");
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.CANCELLED);
        verify(projectRepository).save(any(Project.class));
        verify(auditService).log(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    public void deleteProject_throwsProjectNotFound_whenAlreadyCancelled() {
        project.setStatus(ProjectStatus.CANCELLED);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(securityUtil.getCurrentUserDetails()).thenReturn(new CustomUserDetails(user));
        assertThatThrownBy(() -> projectServiceImpl.deleteProject(project.getId()))
                .isInstanceOf(ProjectNotFound.class)
                .hasMessageContaining(project.getId().toString());
        verify(projectRepository, never()).save(any());
        verify(auditService, never()).log(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    public void deleteProject_throwsInvalidProjectStatusTransition_whenCompleted() {
        project.setStatus(ProjectStatus.COMPLETED);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(securityUtil.getCurrentUserDetails()).thenReturn(new CustomUserDetails(user));

        assertThatThrownBy(() -> projectServiceImpl.deleteProject(project.getId()))
                .isInstanceOf(InvalidProjectStatusTransition.class)
                .hasMessageContaining(project.getId().toString());
        verify(projectRepository, never()).save(any());
        verify(auditService, never()).log(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    public void deleteProject_throwsProjectNotFound_whenProjectDoesNotExist() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> projectServiceImpl.deleteProject(project.getId()))
                .isInstanceOf(ProjectNotFound.class)
                .hasMessageContaining(project.getId().toString());
        verify(projectRepository, never()).save(any());
    }

    @Test
    public void addUserToProject_addsUser_whenValid() {
        project.setStatus(ProjectStatus.ACTIVE);
        project.setMembers(new HashSet<>());
        user.setActive(true);
        user.setProjects(new HashSet<>());
        Set<RoleTypes> roles = Set.of(RoleTypes.USER);

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(securityUtil.getCurrentUserDetails()).thenReturn(new CustomUserDetails(user));
        when(userService.findUserById(user.getId())).thenReturn(user);
        when(userService.hasRole(any(), eq(RoleTypes.USER))).thenReturn(true);

        UserAssignResponseDTO result = projectServiceImpl.addUserToProject(user.getId(), project.getId());

        assertThat(result).isNotNull();
        assertThat(user.getProjects()).contains(project);
        assertThat(project.getMembers()).contains(user);
        verify(userRepository).save(user);
        verify(auditService).log(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    public void addUserToProject_throwsProjectNotFound_whenProjectDoesNotExist() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> projectServiceImpl.addUserToProject(user.getId(), project.getId()))
                .isInstanceOf(ProjectNotFound.class)
                .hasMessageContaining(project.getId().toString());
        verify(userRepository, never()).save(any());
        verify(auditService, never()).log(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    public void addUserToProject_throwsAccessDenied_whenUnAuthorized() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        doThrow(new AccessDeniedException("You are not authorized to access this project"))
                .when(authorizationService).validateProjectOwnership(project);

        assertThatThrownBy(() -> projectServiceImpl.addUserToProject(user.getId(), project.getId()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("not authorized");
        verify(userRepository, never()).save(any());
        verify(auditService, never()).log(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    public void addUserToProject_throwsProjectInActiveException_whenProjectCompleted() {
        project.setStatus(ProjectStatus.COMPLETED);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectServiceImpl.addUserToProject(user.getId(), project.getId()))
                .isInstanceOf(ProjectInActiveException.class);
        verify(userRepository, never()).save(any());
        verify(auditService, never()).log(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    public void addUserToProject_throwsProjectInActiveException_whenProjectCancelled() {
        project.setStatus(ProjectStatus.CANCELLED);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectServiceImpl.addUserToProject(user.getId(), project.getId()))
                .isInstanceOf(ProjectInActiveException.class);
        verify(userRepository, never()).save(any());
        verify(auditService, never()).log(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    public void addUserToProject_throwsInActiveUserException_whenUserIsInactive() {
        project.setStatus(ProjectStatus.ACTIVE);
        user.setActive(false);

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(userService.findUserById(user.getId())).thenReturn(user);
        when(securityUtil.getCurrentUserDetails()).thenReturn(new CustomUserDetails(user));

        assertThatThrownBy(() -> projectServiceImpl.addUserToProject(user.getId(), project.getId()))
                .isInstanceOf(InActiveUserException.class);
        verify(userRepository, never()).save(any());
        verify(auditService, never()).log(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    public void addUserToProject_throwsInvalidProjectMemberException_whenUserIsNotDeveloperRole() {
        project.setStatus(ProjectStatus.ACTIVE);
        user.setActive(true);

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(userService.findUserById(user.getId())).thenReturn(user);
        when(userService.hasRole(any(), eq(RoleTypes.USER))).thenReturn(false);
        when(securityUtil.getCurrentUserDetails()).thenReturn(new CustomUserDetails(user));

        assertThatThrownBy(() -> projectServiceImpl.addUserToProject(user.getId(), project.getId()))
                .isInstanceOf(InvalidProjectMemberException.class);
        verify(userRepository, never()).save(any());
        verify(auditService, never()).log(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    public void addUserToProject_throwsUserAlreadyPartOfProjectException_whenAlreadyMember() {
        project.setStatus(ProjectStatus.ACTIVE);
        user.setActive(true);
        user.getProjects().add(project);

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(userService.findUserById(user.getId())).thenReturn(user);
        when(userService.hasRole(any(), eq(RoleTypes.USER))).thenReturn(true);
        when(securityUtil.getCurrentUserDetails()).thenReturn(new CustomUserDetails(user));

        assertThatThrownBy(() -> projectServiceImpl.addUserToProject(user.getId(), project.getId()))
                .isInstanceOf(UserAlreadyPartOfProjectException.class)
                .hasMessageContaining(project.getId().toString());
        verify(userRepository, never()).save(any());
        verify(auditService, never()).log(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    public void removeUserFromProject_removesUser_whenValid() {
        project.setStatus(ProjectStatus.ACTIVE);
        project.getMembers().add(user);
        user.getProjects().add(project);
        project.setCreatedBy(user);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(securityUtil.getCurrentUserDetails()).thenReturn(new CustomUserDetails(user));
        when(userService.findUserById(user.getId())).thenReturn(user);
        when(userService.hasRole(any(), eq(RoleTypes.USER))).thenReturn(true);
        when(taskRepository.findByAssignedTo_IdAndProject_Id(user.getId(), project.getId()))
                .thenReturn(List.of(task));

        ProjectResponseDTO result = projectServiceImpl.removeUserFromProject(user.getId(), project.getId());

        assertThat(result).isNotNull();
        assertThat(user.getProjects()).doesNotContain(project);
        assertThat(project.getMembers()).doesNotContain(user);
        verify(taskRepository).saveAll(List.of(task));
        verify(userRepository).save(user);
        verify(projectRepository).save(project);
        verify(auditService).log(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    public void removeUserFromProject_throwsProjectNotFound_whenProjectDoesNotExist() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> projectServiceImpl.removeUserFromProject(user.getId(), project.getId()))
                .isInstanceOf(ProjectNotFound.class)
                .hasMessageContaining(project.getId().toString());
        verify(auditService, never()).log(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    public void removeUserFromProject_throwsAccessDenied_whenUnAuthorized() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        doThrow(new AccessDeniedException("You are not authorized to access this project"))
                .when(authorizationService).validateProjectOwnership(project);

        assertThatThrownBy(() -> projectServiceImpl.removeUserFromProject(user.getId(), project.getId()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("not authorized");
        verify(auditService, never()).log(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    public void removeUserFromProject_throwsProjectInActiveException_whenCancelled() {
        project.setStatus(ProjectStatus.CANCELLED);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectServiceImpl.removeUserFromProject(user.getId(), project.getId()))
                .isInstanceOf(ProjectInActiveException.class)
                .hasMessageContaining(project.getId().toString());
        verify(auditService, never()).log(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    public void removeUserFromProject_throwsProjectInActiveException_whenCompleted() {
        project.setStatus(ProjectStatus.COMPLETED);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectServiceImpl.removeUserFromProject(user.getId(), project.getId()))
                .isInstanceOf(ProjectInActiveException.class)
                .hasMessageContaining(project.getId().toString());
        verify(auditService, never()).log(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    public void removeUserFromProject_throwsInvalidProjectMemberException_whenUserIsNotDeveloperRole() {
        project.setStatus(ProjectStatus.ACTIVE);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(userService.findUserById(user.getId())).thenReturn(user);
        when(userService.hasRole(any(), eq(RoleTypes.USER))).thenReturn(false);
        when(securityUtil.getCurrentUserDetails()).thenReturn(new CustomUserDetails(user));

        assertThatThrownBy(() -> projectServiceImpl.removeUserFromProject(user.getId(), project.getId()))
                .isInstanceOf(InvalidProjectMemberException.class);
        verify(auditService, never()).log(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    public void removeUserFromProject_throwsUserDoesNotBelongToSameProject_whenNotAMember() {
        project.setStatus(ProjectStatus.ACTIVE);
        user.setProjects(new HashSet<>());

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(userService.findUserById(user.getId())).thenReturn(user);
        when(userService.hasRole(any(), eq(RoleTypes.USER))).thenReturn(true);
        when(securityUtil.getCurrentUserDetails()).thenReturn(new CustomUserDetails(user));

        assertThatThrownBy(() -> projectServiceImpl.removeUserFromProject(user.getId(), project.getId()))
                .isInstanceOf(UserDoesNotBelongToSameProject.class)
                .hasMessageContaining(user.getId().toString());
        verify(auditService, never()).log(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    public void updateProject_logsCorrectOldAndNewNameValues() {
        project.setStatus(ProjectStatus.ACTIVE);
        project.setName("Old name");
        ProjectUpdateDTO updateDTO = new ProjectUpdateDTO();
        updateDTO.setName("New name");
        project.setCreatedBy(user);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(securityUtil.getCurrentUserDetails()).thenReturn(new CustomUserDetails(user));

        ArgumentCaptor<String> oldValueCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> newValueCaptor = ArgumentCaptor.forClass(String.class);

        projectServiceImpl.updateProject(project.getId(), updateDTO);

        verify(auditService).log(
                eq(AuditEntityType.PROJECT), eq(project.getId()), eq(AuditAction.UPDATE), eq("Name"),
                oldValueCaptor.capture(), newValueCaptor.capture(), any()
        );

        assertThat(oldValueCaptor.getValue()).isEqualTo("Old name");
        assertThat(newValueCaptor.getValue()).isEqualTo("New name");
    }
}