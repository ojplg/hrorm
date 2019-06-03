package org.hrorm.examples.back_reference_parents;

import lombok.Data;

import java.util.List;

@Data
public class SimpleParent {
    Long id;
    String name;
    List<SimpleChild> children;

    public SimpleChild getChildNamed(String childName){
        for(SimpleChild child : children){
            if ( child.getName().equalsIgnoreCase(childName) ){
                return child;
            }
        }
        return null;
    }
}
