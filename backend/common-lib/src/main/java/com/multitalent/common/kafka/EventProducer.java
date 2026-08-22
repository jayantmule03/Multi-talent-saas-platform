package com.multitalent.common.kafka;

import com.multitalent.common.event.BaseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Thin wrapper around KafkaTemplate used by producing services
 * (auth-service, project-service) to publish domain events.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishEmailEvent(BaseEvent event) {
        publish(KafkaTopics.EMAIL_EVENTS, event);
    }

    public void publishAuditEvent(BaseEvent event) {
        publish(KafkaTopics.AUDIT_EVENTS, event);
    }

    public void publishAnalyticsEvent(BaseEvent event) {
        publish(KafkaTopics.ANALYTICS_EVENTS, event);
    }

    private void publish(String topic, BaseEvent event) {
        kafkaTemplate.send(topic, event.getTenantId(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish event {} to topic {}", event.getEventType(), topic, ex);
                    } else {
                        log.info("Published event {} to topic {} (partition={}, offset={})",
                                event.getEventType(), topic,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
