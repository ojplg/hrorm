package org.hrorm.examples;

import lombok.Data;

import java.util.List;

@Data
public class Child {
    private Long id;
    private Long parentId;
    private Long number;
    private List<Grandchild> grandchildList;
}
