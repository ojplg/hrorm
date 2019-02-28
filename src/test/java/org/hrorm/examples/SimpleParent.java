package org.hrorm.examples;

import lombok.Data;

import java.util.List;

@Data
public class SimpleParent {
    Long id;
    String name;
    List<SimpleChild> children;
}
