package org.hrorm.examples.immutables.zoo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Parrot {

    private final Long id;
    private final String name;

    public static Parrot newParrot(String name){
        return new Parrot(null, name);
    }
}
