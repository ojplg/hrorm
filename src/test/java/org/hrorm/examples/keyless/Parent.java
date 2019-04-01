package org.hrorm.examples.keyless;

import lombok.Data;

import java.util.List;

@Data
public class Parent {
    private Long id;
    private String name;
    private List<UnkeyedThing> children;
}
