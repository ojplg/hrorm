package org.hrorm.examples.keyless;

import lombok.Data;

@Data
public class UnkeyedThing {
    String name;
    Sibling sibling;
}
