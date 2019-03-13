package org.hrorm.examples.siblings;

import lombok.Data;

@Data
public class Thing {
    private Long id;
    private String name;
    private Sibling sibling;
}
