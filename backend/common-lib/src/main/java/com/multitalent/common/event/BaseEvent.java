package com.multitalent.common.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * Common envelope fields shared by all domain events published to Kafka.
 * This class (and its subclasses) is the wire contract between services —
 * every producer and consumer depends on common-lib so they always agree
 * on the shape of an event.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseEvent {
    private String eventType;
    private String tenantId;
    private Instant occurredAt;
}
