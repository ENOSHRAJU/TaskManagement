package com.example.taskManagement.Model;

import com.example.taskManagement.Enums.AuditAction;
import com.example.taskManagement.Enums.AuditEntityType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "audit_id")
    private UUID id;

    @Column(name = "entity_type")
    @Enumerated(EnumType.STRING)
    private AuditEntityType entityType;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Column(name = "action")
    @Enumerated(EnumType.STRING)
    private AuditAction action;

    @Column(name = "field_name")
    private String fieldName;

    @Column(name = "old_property_value")
    private String oldPropertyValue;

    @Column(name = "new_property_value")
    private String newPropertyValue;

    @Column(name = "performed_by")
    private UUID performedBy;

    @Column(name = "timeStamp", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
