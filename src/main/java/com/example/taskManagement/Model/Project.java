package com.example.taskManagement.Model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_name", nullable = false)
    private String name;

    @Column(name = "project_description")
    private String description;

    @Enumerated(EnumType.STRING)
    private ProjectStatus status;

    @ManyToOne
    @JoinColumn(name = "create_by")
    private User createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks;

    @OneToMany(mappedBy = "project")
    private Set<User> members;

    @PrePersist
    public void prePersist() {
        if(this.status == null) this.status = ProjectStatus.STARTED;
        this.createdAt = LocalDateTime.now();
    }

}
