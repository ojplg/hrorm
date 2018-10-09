package org.hrorm.examples;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Inventory {
    Long id;
    LocalDateTime date;
    List<Stock> stocks;
}
