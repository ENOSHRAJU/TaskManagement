package com.example.taskManagement.Repository;

import com.example.taskManagement.Model.Project;
import com.example.taskManagement.Enums.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID>, JpaSpecificationExecutor<Project> {

    List<Project> findByStatus(ProjectStatus status);
    List<Project> findByStatusNot(ProjectStatus status);
    List<Project> findByCreatedBy_IdAndStatusNot(UUID userId, ProjectStatus status);
    List<Project> findByCreatedBy_IdAndStatus(UUID userId, ProjectStatus status);
    List<Project> findByMembers_IdAndStatus(UUID userId, ProjectStatus status);

}
