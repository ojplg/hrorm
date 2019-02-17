package org.hrorm;

/**
 * Representation of various aggregation functions that a database can perform.
 */
public class SqlFunction {

    public static final SqlFunction COUNT = new SqlFunction("COUNT");
    public static final SqlFunction SUM = new SqlFunction("SUM");
    public static final SqlFunction MIN = new SqlFunction("MIN");
    public static final SqlFunction MAX = new SqlFunction("MAX");

    private final String functionName;

    private SqlFunction(String functionName){
        this.functionName = functionName;
    }

    public String getFunctionName() {
        return functionName;
    }
}
