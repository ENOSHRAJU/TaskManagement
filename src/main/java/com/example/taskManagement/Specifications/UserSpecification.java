package com.example.taskManagement.Specifications;

import com.example.taskManagement.Model.User;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class UserSpecification {

    public static Specification<User> belongsToProject(UUID projectId) {
        return (root, query, criteriaBuilder) -> {
            query.distinct(true);
            if(projectId == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.join("projects").get("id"), projectId);
        };
    }

    public static Specification<User> containsSearch(String search) {
        return (root, query, criteriaBuilder) -> {
            if(search == null || search.isBlank()) return criteriaBuilder.conjunction();
            String pattern = "%" + search.toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), pattern)
            );
        };
    }
}
