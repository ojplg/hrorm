package org.hrorm.examples.join_with_children;

import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class Stem {

    private Long id;
    private String tag;
    private Pod pod;

    public String getPodMark(){
        return this.pod.getMark();
    }

    public List<String> getPeaFlags(){
        if (pod == null){
            return Collections.emptyList();
        }
        if ( pod.getPeas() == null){
            return Collections.emptyList();
        }
        return this.pod.getPeas().stream().map(Pea::getFlag).collect(Collectors.toList());
    }

}
