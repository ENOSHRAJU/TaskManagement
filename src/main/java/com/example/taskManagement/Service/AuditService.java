package com.example.taskManagement.Service;

import com.example.taskManagement.Enums.AuditAction;
import com.example.taskManagement.Enums.AuditEntityType;
import org.springframework.stereotype.Service;

import java.util.UUID;


public interface AuditService {
    void log(AuditEntityType auditEntityType,
             UUID entityId,
             AuditAction action,
             String fieldName,
             String oldPropertyValue,
             String newPropertyValue,
             UUID performedBy
    );
}
