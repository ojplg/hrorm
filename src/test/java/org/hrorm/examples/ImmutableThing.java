package org.hrorm.examples;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Builder
@Data
public class ImmutableThing {
    private final Long id;
    private final String word;
    private final BigDecimal amount;
}
