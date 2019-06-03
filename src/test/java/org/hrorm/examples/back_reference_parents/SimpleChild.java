package org.hrorm.examples.back_reference_parents;

import lombok.Data;

@Data
public class SimpleChild {
    Long id;
    String name;
    SimpleParent parent;

    @Override
    public String toString() {
        return "SimpleChild{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
