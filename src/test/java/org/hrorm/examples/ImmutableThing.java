package org.hrorm.examples;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Builder
@Data
public class ImmutableThing {
    private final Long id;
    private final String word;
    private final BigDecimal amount;
    private final List<ImmutableChild> children;
}
