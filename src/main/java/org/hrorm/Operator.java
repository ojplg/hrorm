package org.hrorm;

public class Operator {

    public static Operator EQUALS = new Operator("=");

    public static Operator LIKE = new Operator("LIKE");

    public static Operator LESS_THAN = new Operator("<");
    public static Operator LESS_THAN_OR_EQUALS = new Operator("<=");
    public static Operator GREATER_THAN = new Operator(">");
    public static Operator GREATER_THAN_OR_EQUALS = new Operator(">=");

    private final String sqlString;

    private Operator(String sqlString){
        this.sqlString = sqlString;
    }

    public String getSqlString(){
        return sqlString;
    }
}
