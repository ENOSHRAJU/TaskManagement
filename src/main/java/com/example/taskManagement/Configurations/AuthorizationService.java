package com.example.taskManagement.Configurations;

import com.example.taskManagement.Model.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthorizationService {

    private final SecurityUtil securityUtil;
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationService.class);

    public AuthorizationService(SecurityUtil securityUtil) {
        this.securityUtil = securityUtil;
    }

    public void validateAdminAccess() {
        if(securityUtil.isAdmin()) return;

        UUID userId = securityUtil.getCurrentUserDetails().getId();
        LOGGER.warn("User tried to access admin only resource: {}", userId);
        throw new AccessDeniedException("You are not authorized to access this resource");
    }

    // Checks if the user is part of the project or not to make the changes - only for admin and manager
    public void validateProjectOwnership(Project project) {
        if(securityUtil.isAdmin()) return;
        UUID userId = securityUtil.getCurrentUserDetails().getId();
        if(!project.getCreatedBy().getId().equals(userId))  {
            LOGGER.warn("User: {} attempted to modify project without ownership: {}", userId, project.getId());
            throw new AccessDeniedException("You are not authorized to access this project");
        }
    }

    // Checks if the users are part of the project or not
    public void validateProjectAccess(Project project) {
        if(securityUtil.isAdmin()) return;
        UUID userId = securityUtil.getCurrentUserDetails().getId();
        if(project.getCreatedBy().getId().equals(userId)) return;

        //Check whether user is part of project or not
        boolean member = project.getMembers().stream()
                .anyMatch(user -> user.getId().equals(userId));

        if(!member) {
            LOGGER.warn("User: {} tried to access unauthorized project: {}", userId, project.getId());
            throw new AccessDeniedException("You are not authorized to access this project: "+ project.getId());
        }
    }

    // checks if user is authorized to make changes
    public void validateProjectManagementAccess() {
        UUID userId = securityUtil.getCurrentUserDetails().getId();
        if(!securityUtil.isAdmin() && !securityUtil.isManager()) {
            LOGGER.warn("Un-Authorised user: {} tried to create a project", userId);
            throw new AccessDeniedException("You are not authorised to perform this action");
        }
    }
}

