package org.hrorm.examples.siblings;

import lombok.Data;
import org.hrorm.examples.EnumeratedColor;

@Data
public class Cousin {
    private Long id;
    private EnumeratedColor color;
    private SecondCousin secondCousin;
}
