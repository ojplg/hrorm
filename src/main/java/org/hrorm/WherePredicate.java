package org.hrorm;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

/**
 * Represents a particular predicate for filtering results. For example
 * whether the values in a particular numeric column are less than some
 * value.
 *
 * <p>
 *
 * Most users of hrorm will have no need to directly use this.
 *
 * @param <T> the type held by the column being compared
 */
public class WherePredicate<T> {

    public static WherePredicate<Boolean> forBoolean(String columnName, Operator operator, Boolean value) {
        return new WherePredicate<>(columnName, operator, value, PreparedStatement::setBoolean);
    }

    public static WherePredicate<Long> forLong(String columnName, Operator operator, Long value) {
        return new WherePredicate<>(columnName, operator, value, PreparedStatement::setLong);
    }

    public static WherePredicate<String> forString(String columnName, Operator operator, String value) {
        return new WherePredicate<>(columnName, operator, value, PreparedStatement::setString);
    }

    public static WherePredicate<BigDecimal> forBigDecimal(String columnName, Operator operator, BigDecimal value) {
        return new WherePredicate<>(columnName, operator, value, PreparedStatement::setBigDecimal);
    }

    public static WherePredicate<Instant> forInstant(String columnName, Operator operator, Instant value) {
        return new WherePredicate<>(columnName, operator, value,
                (preparedStatement, index, instant) ->
                    {
                        Timestamp sqlTime = Timestamp.from(instant);
                        preparedStatement.setTimestamp(index, sqlTime);
                    });
    }

//    public static <V> WherePredicate<V> forGenericColumn(String name, GenericColumn<V> column, Operator operator, V value){
//        return new WherePredicate<>(name, operator, value, column::setPreparedStatement);
//    }

    private final String columnName;
    private final Operator operator;
    private final T value;
    private final PreparedStatementSetter<T> setter;

    private final Boolean nullityCheck;

    public WherePredicate(String columnName, boolean nullityCheck){
        this.columnName = columnName;
        this.operator = null;
        this.value = null;
        this.setter = (preparedStatement, index, t) -> {};
        this.nullityCheck = nullityCheck;
    }

    public WherePredicate(String columnName, Operator operator, T value, PreparedStatementSetter<T> setter) {
        this.columnName = columnName;
        this.operator = operator;
        this.value = value;
        this.setter = setter;
        this.nullityCheck = null;
    }

    /**
     * Creates a string representing a SQL snippet that can be used to build
     * a prepared statement.
     *
     * @param prefix The prefix to give to the column name to specify its table.
     * @return the SQL snippet.
     */
    public String render(String prefix){
        if( nullityCheck == Boolean.TRUE ){
            return prefix + columnName + " IS NULL ";
        }
        if( nullityCheck == Boolean.FALSE ){
            return prefix + columnName + " IS NOT NULL ";
        }

        return prefix + columnName + " " + operator.getSqlString() + " ? ";
    }

    /**
     * Applies the value held by this object to the passed statement.
     *
     * @param index the index of the variable to be set
     * @param statement the statement being populated
     * @throws SQLException on an error
     */
    public void setValue(int index, PreparedStatement statement) throws SQLException {
        setter.apply(statement, index, value);
    }

}
