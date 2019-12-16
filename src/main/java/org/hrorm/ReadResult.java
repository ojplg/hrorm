package org.hrorm;

import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

public class ReadResult<BUILDER> {

    private static final Logger logger = Logger.getLogger("org.hrorm");

    public static final ReadResult EMPTY = new ReadResult(Envelope.EMPTY, Collections.emptyMap(), () -> {});

    private final Envelope<BUILDER> joinedBuilder;
    private final Map<String,PopulateResult> subResults;
    private final Runnable atCompletion;

    public ReadResult(Envelope<BUILDER> joinedBuilder,
                      Map<String,PopulateResult> subResults,
                      Runnable complete) {
        this.joinedBuilder = joinedBuilder;
        this.subResults = subResults;
        this.atCompletion = complete;
    }

    public Envelope<BUILDER> getJoinedBuilder(){
        return joinedBuilder;
    }

    public Map<String,PopulateResult> getSubResults(){
        return subResults;
    }

    public void complete(){
        logger.warning("completing " + joinedBuilder);

        for(PopulateResult populateResult : subResults.values()){
            if ( populateResult.isJoinedItemResult() ){
                ReadResult subResult = populateResult.getReadResult();
                subResult.complete();
            }
        }

        this.atCompletion.run();
    }

    public Long getId(){
        return joinedBuilder.getId();
    }

    public BUILDER getBuilder(){
        return joinedBuilder.getItem();
    }

}
