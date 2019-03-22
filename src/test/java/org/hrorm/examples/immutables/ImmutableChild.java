package org.hrorm.examples.immutables;

import java.time.LocalDateTime;

@lombok.Builder
@lombok.Data
public class ImmutableChild {
    private final Long id;
    private final LocalDateTime birthday;
    private final Boolean flag;
    private final ImmutableSibling immutableSibling;
}
