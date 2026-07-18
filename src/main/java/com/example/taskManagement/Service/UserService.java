package com.example.taskManagement.Service;

import com.example.taskManagement.Model.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {
    User findUserById(UUID userId);
    User findUserByEmail(String email);
    boolean existsUserByEmail(String email);
    List<String> getUserRoles(UserDetails userDetails);
    String getHighestAuthRole(List<String> roles);
}
