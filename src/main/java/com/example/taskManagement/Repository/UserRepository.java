package com.example.taskManagement.Repository;

import com.example.taskManagement.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);
    Optional<User> findByIdAndActiveTrue(UUID userId);
    boolean existsByEmail(String email);
    //Long countByProjectId(Long projectId);
    @Query("SELECT u FROM User u JOIN FETCH u.roles WHERE u.email = :email AND u.active = true")
    Optional<User> findActiveUserWithRolesByEmail(String email);

}
