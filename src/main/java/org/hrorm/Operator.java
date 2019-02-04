package org.hrorm;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * The <code>Operator</code> type supports the generation of SQL statements
 * that specify the various comparisons allows in SQL where clauses.
 */
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

    /**
     * An instance that represents the equality ('=') operator.
     */
    public static final Operator EQUALS = new Operator("=");

    /**
     * An instance that represents the 'LIKE' operator to be used
     * for string fields. The ampersand character is a wildcard in
     * SQL, matching zero or more of any characters.
     */
    public static final Operator LIKE = new Operator("LIKE");

    /**
     * An instance that represents the less than ('&lt;') operator.
     */
    public static final Operator LESS_THAN = new Operator("<");

    /**
     * An instance that represents the less than or equals ('&lt;=') operator.
     */
    public static final Operator LESS_THAN_OR_EQUALS = new Operator("<=");

    /**
     * An instance that represents the greater than ('&gt;') operator.
     */
    public static final Operator GREATER_THAN = new Operator(">");

    /**
     * An instance that represents the greater than or equals ('&gt;=') operator.
     */
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

    /**
     * To search for items in a range of values, not-inclusive of the endpoints,
     * where the field is an integer type.
     *
     * <p>
     *      Hrorm will search for all records where the field value
     *      is greater than the one included in the template object,
     *      and less than the upper limit specified here.
     * </p>
     *
     * @param upperLimit The endpoint of the range to search.
     * @return An instance representing a range.
     */
    public static Operator openRangeTo(long upperLimit){
        return new Operator(OPEN_RANGE, upperLimit);
    }

    /**
     * To search for items in a range of values, inclusive of the endpoints,
     * where the field is an integer type.
     *
     * <p>
     *      Hrorm will search for all records where the field value
     *      is greater than or equal to the one included in the template object,
     *      and less than or equal to the upper limit specified here.
     * </p>
     *
     * @param upperLimit The endpoint of the range to search.
     * @return An instance representing a range.
     */
    public static Operator closedRangeTo(long upperLimit){
        return new Operator(CLOSED_RANGE, upperLimit);
    }

    /**
     * To search for items in a range of values, not-inclusive of the endpoints,
     * where the field is a de
     * cimal type.
     *
     * <p>
     *      Hrorm will search for all records where the field value
     *      is greater than the one included in the template object,
     *      and less than the upper limit specified here.
     * </p>
     *
     * @param upperLimit The endpoint of the range to search.
     * @return An instance representing a range.
     */
    public static Operator openRangeTo(BigDecimal upperLimit){
        return new Operator(OPEN_RANGE, upperLimit);
    }

    /**
     * To search for items in a range of values, inclusive of the endpoints,
     * where the field is a decimal type.
     *
     * <p>
     *      Hrorm will search for all records where the field value
     *      is greater than or equal to the one included in the template object,
     *      and less than or equal to the upper limit specified here.
     * </p>
     *
     * @param upperLimit The endpoint of the range to search.
     * @return An instance representing a range.
     */
    public static Operator closedRangeTo(BigDecimal upperLimit){
        return new Operator(CLOSED_RANGE, upperLimit);
    }

    /**
     * To search for items in a range of values, not-inclusive of the endpoints,
     * where the field is a date type.
     *
     * <p>
     *      Hrorm will search for all records where the field value
     *      is greater than the one included in the template object,
     *      and less than the upper limit specified here.
     * </p>
     *
     * @param upperLimit The endpoint of the range to search.
     * @return An instance representing a range.
     */
    public static Operator openRangeTo(LocalDateTime upperLimit){
        return new Operator(OPEN_RANGE, upperLimit);
    }

    /**
     * To search for items in a range of values, inclusive of the endpoints,
     * where the field is a date type.
     *
     * <p>
     *      Hrorm will search for all records where the field value
     *      is greater than or equal to the one included in the template object,
     *      and less than or equal to the upper limit specified here.
     * </p>
     *
     * @param upperLimit The endpoint of the range to search.
     * @return An instance representing a range.
     */
    public static Operator closedRangeTo(LocalDateTime upperLimit) {
        return new Operator(CLOSED_RANGE, upperLimit);
    }

    /**
     * Returns the text to be inserted in the SQL statement being built.
     * This is not generally useful to hrorm clients.
     *
     * @param columnName The name of the column this operator applies to.
     * @return The text to insert into the SQL.
     */
    public String getSqlString(String columnName){
        return sqlString.replace("@", columnName);
    }

    /**
     * Indication of whether this operator requires two parameters to
     * be set (it represents a range).
     *
     * <p>Used internally by hrorm, and generally of no interest to clients.</p>
     *
     * @return true if this operator has a second parameter
     */
    public boolean hasSecondParameter(){
        return upperLimit != null;
    }

    /**
     * Sets the second parameter onto the passed SQL statement.
     *
     * <p>Used internally by hrorm, and generally of no interest to clients.</p>
     *
     * @param index The place in the SQL of the parameter being set.
     * @param statement The SQL
     * @throws SQLException on a database error
     */
    public void setSecondParameter(int index, PreparedStatement statement) throws SQLException {
        if ( upperLimit != null ) {
            upperLimit.setValue(index, statement);
        } else {
            throw new HrormException("Attempt to set a second parameter at index " + index +
                    " on statement " + statement + " but no value is known.");
        }
    }
}
