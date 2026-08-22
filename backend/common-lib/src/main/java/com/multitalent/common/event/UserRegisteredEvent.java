package com.multitalent.common.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
public class UserRegisteredEvent extends BaseEvent {
    private String userId;
    private String fullName;
    private String email;
}
