package org.hrorm;

public class WherePredicateAtom<T> {

    private final String columnName;
    private final Operator operator;
    private final T value;

    public WherePredicateAtom(String columnName, Operator operator, T value) {
        this.columnName = columnName;
        this.operator = operator;
        this.value = value;
    }

    public String render(String prefix){
        return prefix + columnName + " " + operator.getSqlString(columnName) + " ? ";
    }

}
