package com.example.taskManagement.Model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(
        indexes = {
                @Index(name = "idx_task_user", columnList = "task_user_id"),
                @Index(name = "idx_task_project", columnList = "task_project_id")
        }
)
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_title", nullable = false)
    private String title;

    @Column(name = "task_description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_status", nullable = false)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(name = "taskCategory", nullable = false)
    private Category taskCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_priority", nullable = false)
    private Priority priority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_user_id")
    private User assignedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_project_id", nullable = false)
    private Project project;

    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    @Column(name = "lastUpdatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    @PrePersist
    public void prePersist() {
        this.status = Status.OPEN;
        this.deleted = false;
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void postPersist() {
        this.updatedAt = LocalDateTime.now();
    }

}
