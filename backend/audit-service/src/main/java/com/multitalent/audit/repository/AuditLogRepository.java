package com.multitalent.audit.repository;

import com.multitalent.audit.document.AuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AuditLogRepository extends MongoRepository<AuditLog, String> {
    List<AuditLog> findByTenantIdOrderByOccurredAtDesc(String tenantId);
    List<AuditLog> findByTenantIdAndEventTypeOrderByOccurredAtDesc(String tenantId, String eventType);
}
