package org.hrorm.examples;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImmutableActor {
    private final Long id;
    private final String name;
}
