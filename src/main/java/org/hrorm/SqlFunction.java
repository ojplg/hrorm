package org.hrorm;

/**
 * Representation of various aggregation functions that a database can perform.
 *
 * <p>
 *     To be used in conjunction with the {@link KeylessDao#runLongFunction}
 *     or {@link KeylessDao#runBigDecimalFunction} methods.
 * </p>
 */
public class SqlFunction {

    /**
     * To run the SQL COUNT function on a column in a table.
     */
    public static final SqlFunction COUNT = new SqlFunction("COUNT");

    /**
     * To run the SQL SUM function on a column in a table.
     */
    public static final SqlFunction SUM = new SqlFunction("SUM");

    /**
     * To run the SQL MIN function on a column in a table.
     */
    public static final SqlFunction MIN = new SqlFunction("MIN");

    /**
     * To run the SQL MAX function on a column in a table.
     */
    public static final SqlFunction MAX = new SqlFunction("MAX");

    /**
     * To run the SQL AVG function on a column in a table.
     */
    public static final SqlFunction AVG = new SqlFunction("AVG");

    private final String functionName;

    private SqlFunction(String functionName){
        this.functionName = functionName;
    }

    public String getFunctionName() {
        return functionName;
    }
}
