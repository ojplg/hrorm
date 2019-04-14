package org.hrorm;

/**
 * The <code>Operator</code> type supports the generation of SQL statements
 * that specify the various comparisons allows in SQL where clauses.
 *
 * <p>
 *     There are no instances for the SQL operators "IS NULL", "IS NOT NULL",
 *     "IN", or "NOT IN". However, all those SQL operations are supported
 *     by the <code>Where</code> class.
 *     Rather than specifying an <code>Operator</code>, directly call
 *     the <code>isNull()</code>, <code>isNotNull()</code>, or the correct
 *     variant of the <code>in()</code> or <code>notIn()</code> method
 *     as appropriate.
 * </p>
 */
public class Operator {

    /**
     * An instance that represents the equality ('=') operator.
     */
    public static final Operator EQUALS = new Operator("=");

    /**
     * An instance that represents the inequality ('&lt;&gt;') operator.
     */
    public static final Operator NOT_EQUALS = new Operator("<>");

    /**
     * An instance that represents the 'LIKE' operator to be used
     * for string fields. The ampersand character is a wildcard in
     * SQL, matching zero or more of any characters.
     */
    public static final Operator LIKE = new Operator("LIKE");

    /**
     * An instance that represents the 'NOT LIKE' operator to be used
     * for string fields. The ampersand character is a wildcard in
     * SQL, matching zero or more of any characters.
     */
    public static final Operator NOT_LIKE = new Operator("NOT LIKE");

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

//    /**
//     *
//     */
//    public static final Operator IN = new Operator("IN");

    private final String sqlString;

    private Operator(String sqlString){
        this.sqlString = sqlString;
    }

    /**
     * Returns the text to be inserted in the SQL statement being built.
     *
     * @return The text to insert into the SQL.
     */
    public String getSqlString(){
        return sqlString;
    }

}
