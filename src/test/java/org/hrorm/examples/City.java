package org.hrorm.examples;

import lombok.Data;

@Data
public class City {
    Long id;
    String name;
    State state;
}
