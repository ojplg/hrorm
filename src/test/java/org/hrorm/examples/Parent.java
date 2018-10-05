package org.hrorm.examples;

import lombok.Data;

import java.util.List;

@Data
public class Parent {
    private Long id;
    private String name;
    private List<Child> childList;
}
