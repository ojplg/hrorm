package org.hrorm;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class Operator {

    private interface UpperLimit {
        void setValue(int index, PreparedStatement statement) throws SQLException;
    }

    private static class LongUpperLimit implements UpperLimit {
        private final Long upperLimit;

        LongUpperLimit(Long upperLimit){
            this.upperLimit = upperLimit;
        }

        @Override
        public void setValue(int index, PreparedStatement statement) throws SQLException {
            statement.setLong(index, upperLimit);
        }
    }

    private static class BigDecimalUpperLimit implements UpperLimit {
        private final BigDecimal upperLimit;

        BigDecimalUpperLimit(BigDecimal upperLimit){
            this.upperLimit = upperLimit;
        }

        @Override
        public void setValue(int index, PreparedStatement statement) throws SQLException {
            statement.setBigDecimal(index, upperLimit);
        }
    }

    private static class LocalDateTimeUpperLimit implements UpperLimit {
        private final LocalDateTime upperLimit;

        LocalDateTimeUpperLimit(LocalDateTime upperLimit){
            this.upperLimit = upperLimit;
        }

        @Override
        public void setValue(int index, PreparedStatement statement) throws SQLException {
            Timestamp sqlTime = Timestamp.valueOf(upperLimit);
            statement.setTimestamp(index, sqlTime);
        }
    }

    public static final Operator EQUALS = new Operator("=");

    public static final Operator LIKE = new Operator("LIKE");

    public static final Operator LESS_THAN = new Operator("<");
    public static final Operator LESS_THAN_OR_EQUALS = new Operator("<=");
    public static final Operator GREATER_THAN = new Operator(">");
    public static final Operator GREATER_THAN_OR_EQUALS = new Operator(">=");

    private static final String OPEN_RANGE =">? and @ <";
    private static final String CLOSED_RANGE =">=? and @ <=";

    private final String sqlString;
    private final UpperLimit upperLimit;

    private Operator(String sqlString){
        this.sqlString = sqlString;
        this.upperLimit = null;
    }

    private Operator(String sqlString, Long upperLimit){
        this.sqlString = sqlString;
        this.upperLimit = new LongUpperLimit(upperLimit);
    }

    private Operator(String sqlString, BigDecimal upperLimit){
        this.sqlString = sqlString;
        this.upperLimit = new BigDecimalUpperLimit(upperLimit);
    }

    private Operator(String sqlString, LocalDateTime upperLimit){
        this.sqlString = sqlString;
        this.upperLimit = new LocalDateTimeUpperLimit(upperLimit);
    }

    public String getSqlString(String columnName){
        return sqlString.replace("@", columnName);
    }

    public static Operator openRangeTo(long upperLimit){
        return new Operator(OPEN_RANGE, upperLimit);
    }

    public static Operator closedRangeTo(long upperLimit){
        return new Operator(CLOSED_RANGE, upperLimit);
    }

    public static Operator openRangeTo(BigDecimal upperLimit){
        return new Operator(OPEN_RANGE, upperLimit);
    }

    public static Operator closedRangeTo(BigDecimal upperLimit){
        return new Operator(CLOSED_RANGE, upperLimit);
    }

    public static Operator openRangeTo(LocalDateTime upperLimit){
        return new Operator(OPEN_RANGE, upperLimit);
    }

    public static Operator closedRangeTo(LocalDateTime upperLimit) {
        return new Operator(CLOSED_RANGE, upperLimit);
    }

    public boolean hasSecondParameter(){
        return upperLimit != null;
    }

    public void setSecondParameter(int index, PreparedStatement statement) throws SQLException {
        upperLimit.setValue(index, statement);
    }
}
