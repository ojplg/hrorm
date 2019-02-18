package org.hrorm;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class WherePredicateAtom<T> {

    @FunctionalInterface
    interface PreparedStatementSetter<VALUE> {
        void apply(PreparedStatement preparedStatement, int index, VALUE value) throws SQLException;
    }

    public static WherePredicateAtom<Long> forLong(String columnName, Operator operator, Long value) {
        PreparedStatementSetter<Long> setter = (preparedStatement, index, aLong) ->
        { preparedStatement.setLong(index, aLong); };
        return new WherePredicateAtom<>(columnName, operator, value, setter);
    }

    public static WherePredicateAtom<String> forString(String columnName, Operator operator, String value) {
        PreparedStatementSetter<String> setter = (preparedStatement, index, v) ->
                                                preparedStatement.setString(index, v);
        return new WherePredicateAtom<>(columnName, operator, value, setter);
    }

    public static WherePredicateAtom<BigDecimal> forBigDecimal(String columnName, Operator operator, BigDecimal value) {
        PreparedStatementSetter<BigDecimal> setter = (preparedStatement, index, v) ->
                preparedStatement.setBigDecimal(index, v);
        return new WherePredicateAtom<>(columnName, operator, value, setter);
    }


    private final String columnName;
    private final Operator operator;
    private final T value;
    private final PreparedStatementSetter<T> setter;



    public WherePredicateAtom(String columnName, Operator operator, T value, PreparedStatementSetter<T> setter) {
        this.columnName = columnName;
        this.operator = operator;
        this.value = value;
        this.setter = setter;
    }

    public String render(String prefix){
        return prefix + columnName + " " + operator.getSqlString(columnName) + " ? ";
    }

    public void setValue(int index, PreparedStatement statement) throws SQLException {
        setter.apply(statement, index, value);
    }

}
