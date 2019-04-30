package org.hrorm.examples.generickeys;

import lombok.Data;

@Data
public class Cake {
    private String name;
    private String flavor;
    private Frosting frosting;
}
