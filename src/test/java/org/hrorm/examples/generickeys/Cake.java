package org.hrorm.examples.generickeys;

import lombok.Data;

import java.util.List;

@Data
public class Cake {
    private String name;
    private String flavor;
    private Frosting frosting;
    private List<Layer> layers;
}
