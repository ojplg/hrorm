package org.hrorm.examples;

import lombok.Data;

@Data
public class Grandchild {
    private Long id;
    private Child child;
    private EnumeratedColor color;
}
