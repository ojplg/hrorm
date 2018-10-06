package org.hrorm.examples;

import lombok.Data;

@Data
public class Sibling {
    private Long id;
    private Long number;
    private Cousin cousin;
}
