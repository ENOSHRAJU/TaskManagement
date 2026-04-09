package com.example.taskManagement.Repository;

import com.example.taskManagement.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    Optional<User> findByIdAndActiveTrue(Long userId);
    boolean existsByEmail(String email);
    //Long countByProjectId(Long projectId);
    @Query("SELECT u FROM User u JOIN FETCH u.roles WHERE u.email = :email AND u.active = true")
    Optional<User> findActiveUserWithRolesByEmail(String email);

}
