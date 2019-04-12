package org.hrorm;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Collections;
import java.util.Set;

/**
 * A column that represents a particular Java type.
 *
 * <p>
 *     This can be used if none of the types Hrorm has built in meet
 *     the needs. The user must provide mechanisms for setting the
 *     type's value onto a <code>java.sql.PreparedStatement</code> and
 *     for reading a value from a <code>java.sql.ResultSet</code>.
 * </p>
 *
 * <p>
 *      For example, to create a column for <code>Integer</code>, do the following:
 * </p>
 *
 * <pre>{@code
 * GenericColumn<Integer> integerColumn = new GenericColumn<>(
 *     PreparedStatement::setInt,
 *     ResultSet::getInt,
 *     java.sql.Types.Integer);
 * }</pre>
 *
 * @param <TYPE> The Java type represented by the column.
 */
public class GenericColumn<TYPE> {

    // built in column types
    public static GenericColumn<Long> LONG =
            new GenericColumn<>(PreparedStatement::setLong, ResultSet::getLong, Types.INTEGER, "integer", ColumnTypes.IntegerTypes);
    public static GenericColumn<BigDecimal> BIG_DECIMAL =
            new GenericColumn<>(PreparedStatement::setBigDecimal, ResultSet::getBigDecimal, Types.DECIMAL, "decimal", ColumnTypes.DecimalTypes);
    public static GenericColumn<Boolean> BOOLEAN =
            new GenericColumn<>(PreparedStatement::setBoolean, ResultSet::getBoolean, Types.BOOLEAN, "boolean", ColumnTypes.BooleanTypes);
    public static GenericColumn<String> STRING =
            new GenericColumn<>(PreparedStatement::setString, ResultSet::getString, Types.VARCHAR, "text", ColumnTypes.StringTypes);

    // extension types
    public static GenericColumn<Integer> INTEGER =
            new GenericColumn<>(PreparedStatement::setInt, ResultSet::getInt, Types.INTEGER, "integer");
    public static GenericColumn<Byte> BYTE =
            new GenericColumn<>(PreparedStatement::setByte, ResultSet::getByte, Types.TINYINT, "tinyint");
    public static GenericColumn<Float> FLOAT =
            new GenericColumn<>(PreparedStatement::setFloat, ResultSet::getFloat, Types.FLOAT, "float");
    public static GenericColumn<Double> DOUBLE =
            new GenericColumn<>(PreparedStatement::setDouble, ResultSet::getDouble, Types.DOUBLE, "double");
    public static GenericColumn<Timestamp> TIMESTAMP =
            new GenericColumn<>(PreparedStatement::setTimestamp, ResultSet::getTimestamp, Types.TIMESTAMP, "timestamp");

    private final Integer sqlType;
    private final Set<Integer> supportedTypes;
    private final String sqlTypeName;
    private final PreparedStatementSetter<TYPE> preparedStatementSetter;
    private final ResultSetReader<TYPE> resultReader;

    /**
     * Create a generic column instance to support the <code>TYPE</code>.
     *
     * @param preparedStatementSetter The method used to set the type onto a prepared statement.
     * @param resultReader The method used to read the value out of a result set.
     * @param sqlType The kind of this column type, as defined in <code>java.sql.Types</code>
     */
    public GenericColumn(PreparedStatementSetter<TYPE> preparedStatementSetter, ResultSetReader<TYPE> resultReader, int sqlType){
        this.sqlType = sqlType;
        this.preparedStatementSetter = preparedStatementSetter;
        this.resultReader = resultReader;
        this.sqlTypeName = "UNSET";
        this.supportedTypes = Collections.singleton(sqlType);
    }

    /**
     * Create a generic column instance to support the <code>TYPE</code>.
     *
     * @param preparedStatementSetter The method used to set the type onto a prepared statement.
     * @param resultReader The method used to read the value out of a result set.
     * @param sqlType The kind of this column type, as defined in <code>java.sql.Types</code>
     * @param sqlTypeName The name of the type in the SQL schema. This optional value can be set
     *                    if you wish to generate your schema using a {@link Schema} object.
     */
    public GenericColumn(PreparedStatementSetter<TYPE> preparedStatementSetter, ResultSetReader<TYPE> resultReader, int sqlType, String sqlTypeName){
        this.sqlType = sqlType;
        this.preparedStatementSetter = preparedStatementSetter;
        this.resultReader = resultReader;
        this.sqlTypeName = sqlTypeName;
        this.supportedTypes = Collections.singleton(sqlType);
    }

    /**
     * Create a generic column instance to support the <code>TYPE</code>.
     *
     * @param preparedStatementSetter The method used to set the type onto a prepared statement.
     * @param resultReader The method used to read the value out of a result set.
     * @param sqlType The kind of this column type, as defined in <code>java.sql.Types</code>
     * @param sqlTypeName The name of the type in the SQL schema. This optional value can be set
     *                    if you wish to generate your schema using a {@link Schema} object.
     * @param supportedTypes All the database types, as defined in <code>java.sql.Types</code>
     *                       that this column can support.
     */
    public GenericColumn(PreparedStatementSetter<TYPE> preparedStatementSetter, ResultSetReader<TYPE> resultReader, int sqlType, String sqlTypeName, Set<Integer> supportedTypes){
        this.sqlType = sqlType;
        this.preparedStatementSetter = preparedStatementSetter;
        this.resultReader = resultReader;
        this.sqlTypeName = sqlTypeName;
        this.supportedTypes = Collections.unmodifiableSet(supportedTypes);
    }


    public TYPE fromResultSet(ResultSet resultSet, String columnName) throws SQLException {
        TYPE value = resultReader.read(resultSet, columnName);
        if( resultSet.wasNull() ){
            return null;
        }
        return value;
    }

    public void setPreparedStatement(PreparedStatement preparedStatement, int index, TYPE value) throws SQLException {
        preparedStatementSetter.apply(preparedStatement, index, value);
    }

    public int sqlType() {
        return sqlType;
    }

    public String getSqlTypeName(){
        return sqlTypeName;
    }

    public Set<Integer> getSupportedTypes() { return supportedTypes; }

}
