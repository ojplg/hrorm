package org.hrorm.examples.join_with_children;

import lombok.Data;

@Data
public class Root {

    private Long id;
    private Long number;
    private Stem stem;

}
