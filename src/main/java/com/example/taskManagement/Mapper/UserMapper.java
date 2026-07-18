package com.example.taskManagement.Mapper;

import com.example.taskManagement.DTOs.UserAssignResponseDTO;
import com.example.taskManagement.DTOs.UserSummaryDTO;
import com.example.taskManagement.Model.Project;
import com.example.taskManagement.Model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public static UserSummaryDTO userSummaryDTO(User user) {
        UserSummaryDTO summaryDTO = new UserSummaryDTO();
        summaryDTO.setId(user.getId());
        summaryDTO.setName(user.getName());
        return summaryDTO;
    }

    public static UserAssignResponseDTO userAssignResponseDTO(User user, Project project) {
        UserAssignResponseDTO assignResponseDTO = new UserAssignResponseDTO();
        assignResponseDTO.setUserId(user.getId());
        assignResponseDTO.setUsername(user.getName());
        assignResponseDTO.setProjectId(project.getId());
        assignResponseDTO.setProjectName(project.getName());
        return assignResponseDTO;
    }
}
