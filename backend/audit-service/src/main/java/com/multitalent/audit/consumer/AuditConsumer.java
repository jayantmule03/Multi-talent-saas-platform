package com.multitalent.audit.consumer;

import com.multitalent.audit.document.AuditLog;
import com.multitalent.audit.service.AuditService;
import com.multitalent.common.event.BaseEvent;
import com.multitalent.common.event.ProjectCreatedEvent;
import com.multitalent.common.event.UserLoggedInEvent;
import com.multitalent.common.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Listens to "audit-events" (published by auth-service and project-service)
 * and persists an immutable trail entry to this service's own MongoDB.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditConsumer {

    private final AuditService auditService;

    @KafkaListener(topics = "audit-events", groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory")
    public void consume(Object event) {
        AuditLog.AuditLogBuilder builder = AuditLog.builder();

        if (event instanceof UserRegisteredEvent e) {
            builder.tenantId(e.getTenantId()).eventType(e.getEventType())
                    .actorId(e.getUserId()).actorEmail(e.getEmail())
                    .description("User registered: " + e.getFullName())
                    .occurredAt(e.getOccurredAt());
        } else if (event instanceof ProjectCreatedEvent e) {
            builder.tenantId(e.getTenantId()).eventType(e.getEventType())
                    .actorId(e.getCreatedByUserId()).actorEmail(e.getCreatedByEmail())
                    .description("Project created: " + e.getProjectName())
                    .occurredAt(e.getOccurredAt());
        } else if (event instanceof UserLoggedInEvent e) {
            builder.tenantId(e.getTenantId()).eventType(e.getEventType())
                    .actorId(e.getUserId()).actorEmail(e.getEmail())
                    .ipAddress(e.getIpAddress())
                    .description("User logged in")
                    .occurredAt(e.getOccurredAt());
        } else if (event instanceof BaseEvent e) {
            builder.tenantId(e.getTenantId()).eventType(e.getEventType())
                    .description("Generic event").occurredAt(e.getOccurredAt());
        } else {
            log.warn("Unrecognized event on audit-events topic: {}", event);
            return;
        }

        AuditLog saved = auditService.record(builder.build());
        log.info("Audit log persisted: {} ({})", saved.getEventType(), saved.getId());
    }
}
