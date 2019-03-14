package org.hrorm.examples.parentage;

import lombok.Data;

import java.util.List;

@Data
public class Parent {
    private Long id;
    private String name;
    private List<Child> childList;

    public Child getChildByNumber(Long number){
        for(Child child : childList){
            if (number.equals(child.getNumber())){
                return child;
            }
        }
        return null;
    }



}
