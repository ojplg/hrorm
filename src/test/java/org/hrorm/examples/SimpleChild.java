package org.hrorm.examples;

import lombok.Data;

@Data
public class SimpleChild {
    Long id;
    String name;

    @Override
    public String toString() {
        return "SimpleChild{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
