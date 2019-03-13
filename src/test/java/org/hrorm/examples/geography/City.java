package org.hrorm.examples.geography;

import lombok.Data;

@Data
public class City {
    Long id;
    String name;
    State state;
}
