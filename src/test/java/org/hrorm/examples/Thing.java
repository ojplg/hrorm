package org.hrorm.examples;

import lombok.Data;

@Data
public class Thing {
    private Long id;
    private String name;
    private Sibling sibling;
}
