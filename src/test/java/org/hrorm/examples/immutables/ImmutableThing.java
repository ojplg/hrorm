package org.hrorm.examples.immutables;

import java.math.BigDecimal;
import java.util.List;

@lombok.Builder
@lombok.Data
public class ImmutableThing {
    private final Long id;
    private final String word;
    private final BigDecimal amount;
    private final List<ImmutableChild> children;
}
