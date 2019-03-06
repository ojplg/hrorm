package org.hrorm.examples;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Product {

    private Long id;
    private String name;
    private ProductCategory category;
    private BigDecimal price;
    private long sku;
    private boolean discontinued;
    private LocalDateTime firstAvailable;
}
