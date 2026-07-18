package com.example.taskManagement.Service;

import com.example.taskManagement.Enums.AuditAction;
import com.example.taskManagement.Enums.AuditEntityType;
import com.example.taskManagement.Model.AuditLog;
import com.example.taskManagement.Repository.AuditRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuditServiceImpl implements AuditService {

    private final AuditRepository auditRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);

    public AuditServiceImpl(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @Override
    public void log(AuditEntityType auditEntityType, UUID entityId, AuditAction action, String fieldName, String oldPropertyValue, String newPropertyValue, UUID performedBy) {
        LOGGER.debug(
                "Creating audit log for entity: {}, entityId: {}, action: {}",
                auditEntityType,
                entityId,
                action
        );
        AuditLog auditLog = AuditLog.builder()
                .entityType(auditEntityType)
                .entityId(entityId)
                .action(action)
                .fieldName(fieldName)
                .oldPropertyValue(oldPropertyValue)
                .newPropertyValue(newPropertyValue)
                .performedBy(performedBy)
                .build();

        auditRepository.save(auditLog);
    }
}
