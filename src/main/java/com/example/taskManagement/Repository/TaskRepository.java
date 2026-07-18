package com.example.taskManagement.Repository;

import com.example.taskManagement.Enums.TaskStatus;
import com.example.taskManagement.Model.Task;
import com.example.taskManagement.Specifications.TaskSpecification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID>, JpaSpecificationExecutor<Task> {

    List<Task> findByDeletedFalse();
    List<Task> findByProjectIdAndDeletedFalse(UUID id);
    List<Task> findByAssignedToIdAndDeletedFalse(UUID id);
    List<Task> findByStatusAndProjectId(TaskStatus status, UUID projectId);
    List<Task> findByProjectId(UUID projectId);
    List<Task> findByAssignedTo_IdAndProject_Id(UUID userId, UUID projectId);
    // For scheduler — finds tasks to mark overdue
    @Query("SELECT t FROM Task t WHERE t.dueDate < :today AND t.status NOT IN ('CLOSED', 'CANCELLED', 'OVERDUE') AND t.deleted = false")
    List<Task> findTasksDueBeforeDate(@Param("today") LocalDate today);

    // For user dashboard — their overdue tasks only
    List<Task> findByAssignedToIdAndStatusAndDeletedFalse(UUID userId, TaskStatus status);

    // For manager dashboard — all overdue tasks
    List<Task> findByStatusAndDeletedFalse(TaskStatus status);
}

