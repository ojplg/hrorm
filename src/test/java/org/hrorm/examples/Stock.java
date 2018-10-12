package org.hrorm.examples;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Stock {
    Long id;
    Inventory inventory;
    String productName;
    BigDecimal amount;
}
