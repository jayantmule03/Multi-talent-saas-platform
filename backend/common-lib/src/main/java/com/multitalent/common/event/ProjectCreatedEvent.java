package com.multitalent.common.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
public class ProjectCreatedEvent extends BaseEvent {
    private String projectId;
    private String projectName;
    private String createdByUserId;
    private String createdByEmail;
}
