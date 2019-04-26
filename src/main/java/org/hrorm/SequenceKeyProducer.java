package org.hrorm;

import java.sql.Connection;

public class SequenceKeyProducer implements KeyProducer<Long> {

    private final SequencedPrimaryKey sequencedPrimaryKey;

    public SequenceKeyProducer(SequencedPrimaryKey sequencedPrimaryKey){
        this.sequencedPrimaryKey = sequencedPrimaryKey;
    }

    @Override
    public Long produceKey(Connection connection) {
        String sequenceSql = nextSequence();
        SqlRunner sqlRunner = new SqlRunner(connection);
        return sqlRunner.runSequenceNextValue(sequenceSql);
    }

    private String nextSequence(){
        if ( sequencedPrimaryKey == null ){
            throw new HrormException("Cannot get sequence value without primary key");
        }
        return "select nextval('" + sequencedPrimaryKey.getSequenceName() + "')";
    }
}
