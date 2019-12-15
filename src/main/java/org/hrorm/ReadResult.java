package org.hrorm;

import java.util.Collections;
import java.util.Map;

public class ReadResult<BUILDER> {

    public static final ReadResult EMPTY = new ReadResult(Envelope.EMPTY, Collections.emptyMap());

    private final Envelope<BUILDER> joinedBuilder;
    private final Map<String,PopulateResult> subResults;

    public ReadResult(Envelope<BUILDER> joinedBuilder, Map<String,PopulateResult> subResults){
        this.joinedBuilder = joinedBuilder;
        this.subResults = subResults;
    }

    public Envelope<BUILDER> getJoinedBuilder(){
        return joinedBuilder;
    }

    public Map<String,PopulateResult> getSubResults(){
        return subResults;
    }

}
