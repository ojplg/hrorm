package org.hrorm.examples;

import lombok.Data;

@Data
public class Cousin {
    private Long id;
    private EnumeratedColor color;
    private SecondCousin secondCousin;
}
