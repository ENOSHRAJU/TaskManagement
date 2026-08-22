package com.example.taskManagement.Service;

import com.example.taskManagement.Enums.RoleTypes;
import com.example.taskManagement.Model.Role;
import com.example.taskManagement.Model.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface UserService {
    boolean hasRole(Set<Role> roles, RoleTypes roleTypes);
    User findUserById(UUID userId);
    User getUserByEmail(String email);
    User findUserByEmail(String email);
    boolean existsUserByEmail(String email);
    List<String> getUserRoles(UserDetails userDetails);
    String getHighestAuthRole(List<String> roles);
    void saveUser(User user);
}
