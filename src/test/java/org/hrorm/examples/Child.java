package org.hrorm.examples;

import lombok.Data;

import java.util.List;

@Data
public class Child {
    private Long id;
    private Parent parent;
    private Long number;
    private List<Grandchild> grandchildList;

    @Override
    public String toString() {
        return "Child{" +
                "id=" + id +
                ", parentId=" + (parent == null ? "null" : parent.getId() ) +
                ", number=" + number +
                ", grandchildList=" + grandchildList +
                '}';
    }
}
