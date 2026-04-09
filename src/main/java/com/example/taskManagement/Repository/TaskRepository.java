package com.example.taskManagement.Repository;

import com.example.taskManagement.Model.Status;
import com.example.taskManagement.Model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByDeletedFalse();
    // Optional<Task> findByIdAndDeletedFalse(Long taskId);
    List<Task> findByProjectIdAndDeletedFalse(Long id);
    List<Task> findByAssignedToIdAndDeletedFalse(Long id);
    List<Task> findByStatusAndProjectId(Status status, Long projectId);

}

