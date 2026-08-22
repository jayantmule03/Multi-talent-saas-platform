package com.multitalent.common.kafka;

/**
 * Kafka topic name constants shared by every producer/consumer so services
 * can't drift apart on topic naming.
 */
public final class KafkaTopics {
    public static final String EMAIL_EVENTS = "email-events";
    public static final String AUDIT_EVENTS = "audit-events";
    public static final String ANALYTICS_EVENTS = "analytics-events";

    private KafkaTopics() {
    }
}
