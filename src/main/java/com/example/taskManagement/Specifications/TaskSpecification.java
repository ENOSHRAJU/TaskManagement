package com.example.taskManagement.Specifications;

import com.example.taskManagement.Enums.TaskCategory;
import com.example.taskManagement.Enums.TaskPriority;
import com.example.taskManagement.Enums.TaskStatus;
import com.example.taskManagement.Model.Task;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class TaskSpecification {

    public static Specification<Task> accessibleTasks(boolean isAdmin, boolean isManager, UUID userId) {
        if(isAdmin) return Specification.where(null);
        if(isManager) return tasksCreatedByManager(userId);
        return assignedTo(userId);
    }

    public static Specification<Task> tasksCreatedByManager(UUID userId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("project").get("createdBy").get("id"), userId);
        };
    }

    public static Specification<Task> hasStatus(TaskStatus statusTypes) {
        return (root, query, criteriaBuilder) -> {
            if(statusTypes == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("status"), statusTypes);
        };
    }

    public static Specification<Task> hasPriority(TaskPriority priority) {
        return (root, query, criteriaBuilder) -> {
            if(priority == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("taskPriority"), priority);
        };
    }

    public static Specification<Task> assignedTo(UUID userId) {
        return (root, query, criteriaBuilder) -> {
            if(userId == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("assignedTo").get("id"), userId);
        };
    }

    public static Specification<Task> hasCategory(TaskCategory category) {
        return (root, query, criteriaBuilder) -> {
            if(category == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("taskCategory"), category);
        };
    }

    public static Specification<Task> containsSearch(String search) {
        return (root, query, criteriaBuilder) -> {
            if(search == null || search.isBlank()) return criteriaBuilder.conjunction();

            String pattern = "%" + search.toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern)
            );
        };
    }

    public static Specification<Task> belongsToProject(UUID projectId) {
        return (root, query, criteriaBuilder) -> {
            if (projectId == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("project").get("id"), projectId);
        };
    }

}
