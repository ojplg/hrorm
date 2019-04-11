package org.hrorm.examples.builtins;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class BuiltIns {
    private Long id;

    private int intValue;
    private byte byteValue;
    private float floatValue;
    private double doubleValue;
    private Timestamp timestampValue;

}
