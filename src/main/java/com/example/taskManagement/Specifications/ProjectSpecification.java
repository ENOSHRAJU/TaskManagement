package com.example.taskManagement.Specifications;

import com.example.taskManagement.Enums.ProjectStatus;
import com.example.taskManagement.Model.Project;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class ProjectSpecification {

    public static Specification<Project> accessibleProjects(boolean isAdmin, boolean isManager, UUID userId) {
        if(isAdmin) return Specification.where(null);
        if(isManager) return projectCreatedBy(userId);
        return projectMember(userId);
    }

    public static Specification<Project> projectMember(UUID userId) {
        return (root, query, criteriaBuilder) -> {
            if(userId == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.join("members").get("id"), userId);
        };
    }

    public static Specification<Project> hasStatus(ProjectStatus status) {
        return (root, query, criteriaBuilder) -> {
            if(status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public static Specification<Project> containsSearch(String search) {
        return (root, query, criteriaBuilder) -> {
            if(search == null || search.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            String pattern = "%" + search.toLowerCase() + "%";

            return criteriaBuilder.or(
                    // Lowercasing the database value to compare with pattern
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern)
            );
        };
    }

    public static Specification<Project> projectCreatedBy(UUID userId) {
        return (root, query, criteriaBuilder) -> {
            if(userId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("createdBy").get("id"), userId);
        };
    }
}
