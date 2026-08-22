package com.example.taskManagement.Service;

import com.example.taskManagement.Enums.RoleTypes;
import com.example.taskManagement.Exception.DuplicateEmailException;
import com.example.taskManagement.Exception.UserNotFound;
import com.example.taskManagement.Model.Role;
import com.example.taskManagement.Model.User;
import com.example.taskManagement.Repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean hasRole(Set<Role> roles, RoleTypes roleType) {
        return roles.stream().anyMatch(role -> role.getRole().equals(roleType.name()));
    }

    @Override
    public User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    LOGGER.warn("User not found in database: " + userId);
                    return new UserNotFound("User not found with id: " + userId);
                });
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    LOGGER.warn("User not found in database with email: " + email);
                    return new UserNotFound("User not found with id: " + email);
                });
    }

    @Override
    public boolean existsUserByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public List<String> getUserRoles(UserDetails userDetails){
        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
    }

    @Override
    public String getHighestAuthRole(List<String> roles) {
        if (roles.contains("ROLE_ADMIN")) return "ADMIN";
        if (roles.contains("ROLE_MANAGER")) return "MANAGER";
        return "USER";
    }

    @Override
    public void saveUser(User user) {
        userRepository.save(user);
    }
}
