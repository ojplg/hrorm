package org.hrorm.examples.join_with_children;

import lombok.Data;

@Data
public class Stem {

    private Long id;
    private String tag;
    private Pod pod;

}
