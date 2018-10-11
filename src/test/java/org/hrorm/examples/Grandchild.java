package org.hrorm.examples;

import lombok.Data;

@Data
public class Grandchild {
    private Long id;
    private Child child;
    private EnumeratedColor color;

    @Override
    public String toString() {
        return "Grandchild{" +
                "id=" + id +
                ", childId=" + (child==null?"null":child.getId()) +
                ", color=" + color +
                '}';
    }
}
