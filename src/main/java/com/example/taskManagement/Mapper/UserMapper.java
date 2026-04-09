package com.example.taskManagement.Mapper;

import com.example.taskManagement.DTOs.UserSummaryDTO;
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
}
