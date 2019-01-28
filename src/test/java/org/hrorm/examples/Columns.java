package org.hrorm.examples;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Columns {
    private Long id;
    private String stringThing;
    private Long integerThing;
    private Boolean booleanThing;
    private LocalDateTime timeStampThing;
    private EnumeratedColor colorThing;
    private BigDecimal decimalThing;
}
