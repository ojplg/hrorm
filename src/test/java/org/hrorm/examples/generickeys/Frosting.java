package org.hrorm.examples.generickeys;

import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
public class Frosting {
    private Timestamp id;
    private BigDecimal amount;
}
