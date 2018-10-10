package org.hrorm.examples;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Stock {
    Long id;
    Long inventoryId;
    String productName;
    BigDecimal amount;
}
