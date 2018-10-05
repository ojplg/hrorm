package org.hrorm.examples;

import lombok.Data;
import org.hrorm.ColumnsTest;

import java.time.LocalDateTime;

@Data
public class Columns {
    private Long id;
    private String stringThing;
    private long integerThing;
    private Boolean booleanThing;
    private LocalDateTime timeStampThing;
    private EnumeratedColor colorThing;
}
