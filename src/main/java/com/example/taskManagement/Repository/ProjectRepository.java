package com.example.taskManagement.Repository;

import com.example.taskManagement.Model.Project;
import com.example.taskManagement.Model.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByStatus(ProjectStatus status);
    List<Project> findByStatusNot(ProjectStatus status);
    List<Project> findByCreatedBy_IdAndStatusNot(Long userId, ProjectStatus status);

}
