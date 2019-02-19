package org.hrorm;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Represents a particular predicate for filtering results. For example
 * whether the values in a particular numeric column are less than some
 * value.
 *
 * @param <T> the type held by the column being compared
 */
public class WherePredicate<T> {

    @FunctionalInterface
    interface PreparedStatementSetter<VALUE> {
        void apply(PreparedStatement preparedStatement, int index, VALUE value) throws SQLException;
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

    private final String columnName;
    private final Operator operator;
    private final T value;
    private final PreparedStatementSetter<T> setter;

    public WherePredicate(String columnName, Operator operator, T value, PreparedStatementSetter<T> setter) {
        this.columnName = columnName;
        this.operator = operator;
        this.value = value;
        this.setter = setter;
    }

    /**
     * Creates a string representing a SQL snippet that can be used to build
     * a prepared statement.
     *
     * @param prefix The prefix to give to the column name to specify its table.
     * @return the SQL snippet.
     */
    public String render(String prefix){
        return prefix + columnName + " " + operator.getSqlString(columnName) + " ? ";
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
