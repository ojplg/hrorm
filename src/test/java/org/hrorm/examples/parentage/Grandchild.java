package org.hrorm.examples.parentage;

import lombok.Data;
import org.hrorm.examples.EnumeratedColor;

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
