package org.hrorm;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

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

    public static <V> WherePredicate<V> forGeneric(String name, Operator operator, V value, GenericColumn<V> column){
        return new WherePredicate<>(name, operator, value, column::setPreparedStatement);
    }

    private final String columnName;
    private final Operator operator;
    private final List<T> values;
    private final PreparedStatementSetter<T> setter;

    private final Boolean nullityCheck;
    private final Boolean inClause;

    public WherePredicate(String columnName, boolean nullityCheck){
        this.columnName = columnName;
        this.operator = null;
        this.values = Collections.emptyList();
        this.setter = (preparedStatement, index, t) -> {};
        this.nullityCheck = nullityCheck;
        this.inClause = null;
    }

    public WherePredicate(String columnName, Operator operator, T value, PreparedStatementSetter<T> setter) {
        this.columnName = columnName;
        this.operator = operator;
        this.values = Collections.singletonList(value);
        this.setter = setter;
        this.nullityCheck = null;
        this.inClause = null;
    }

    public WherePredicate(String columnName, GenericColumn<T> column, List<T> elements){
        this.columnName = columnName;
        this.operator = null;
        this.values = elements;
        this.nullityCheck = null;
        this.setter = column::setPreparedStatement;
        this.inClause = Boolean.TRUE;
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

        if ( inClause == Boolean.TRUE ){
            StringBuilder buf = new StringBuilder();
            buf.append(prefix);
            buf.append(columnName);
            buf.append(" ");
            buf.append("IN");
            buf.append(" ( ");
            buf.append(String.join(", ", Collections.nCopies(values.size(), "?")));
            buf.append(" ) ");
            return buf.toString();
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
    public int setValue(int index, PreparedStatement statement) throws SQLException {
        for(T value : values){
            setter.apply(statement, index, value);
            index++;
        }
        return values.size();
    }

}
