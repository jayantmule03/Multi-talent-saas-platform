package com.multitalent.audit.service;

import com.multitalent.audit.document.AuditLog;
import com.multitalent.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditLog record(AuditLog log) {
        return auditLogRepository.save(log);
    }

    public List<AuditLog> getLogsForTenant(String tenantId) {
        return auditLogRepository.findByTenantIdOrderByOccurredAtDesc(tenantId);
    }
}
