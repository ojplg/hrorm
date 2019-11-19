package org.hrorm.examples.join_with_children;

import lombok.Data;

import java.util.List;

@Data
public class Pod {

    private Long id;
    private String mark;
    private List<Pea> peas;

}
