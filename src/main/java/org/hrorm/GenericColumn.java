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
 * A representation of a database column that directly persists a particular Java type.
 *
 * <p>
 *     For the type it represents this class encapsulates
 * </p>
 *
 * <ul>
 *     <li>
 *         how to set a value of the given type onto a <code>java.sql.PreparedStatement</code>
 *     </li>
 *     <li>
 *         how to read a value of the given type from a <code>java.sql.ResultSet</code>
 *     </li>
 *     <li>
 *         the type of the column, as given in <code>java.sql.Types</code>
 *     </li>
 * </ul>
 *
 * <p>
 *     In addition to creating their own instances, users can access a number
 *     of implementations of this class that are provided as public static
 *     variables.
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
 * <p>
 *     Note that <code>GenericColumn.Integer</code> is essentially that and can
 *     simply be used directly.
 * </p>
 *
 * <p>
 *     Using a custom <code>GenericColumn</code> is easy, as shown in the example above,
 *     but greater care must be taken when using hrorm's facilities for validation ({@link Validator})
 *     or schema generation ({@link Schema}). For example, users may wish to create multiple instances of
 *     this class that all support <code>float</code> member variables, if there are a variety
 *     of sizes of columns in their database, all represented by different SQL variable types.
 * </p>
 *
 * @param <TYPE> The Java type represented by the column.
 */
public class GenericColumn<TYPE> {

    // built in column types

    /**
     * An instance that supports <code>Long</code> or <code>long</code> data elements.
     */
    public static GenericColumn<Long> LONG =
            new GenericColumn<>(PreparedStatement::setLong, ResultSet::getLong, Types.INTEGER, "integer", ColumnTypes.IntegerTypes);

    /**
     * An instance that supports <code>BigDecimal</code> data elements.
     */
    public static GenericColumn<BigDecimal> BIG_DECIMAL =
            new GenericColumn<>(PreparedStatement::setBigDecimal, ResultSet::getBigDecimal, Types.DECIMAL, "decimal", ColumnTypes.DecimalTypes);

    /**
     * An instance that supports <code>Boolean</code> or <code>boolean</code> data elements.
     */
    public static GenericColumn<Boolean> BOOLEAN =
            new GenericColumn<>(PreparedStatement::setBoolean, ResultSet::getBoolean, Types.BOOLEAN, "boolean", ColumnTypes.BooleanTypes);

    /**
     * An instance that supports <code>String</code> data elements.
     */
    public static GenericColumn<String> STRING =
            new GenericColumn<>(PreparedStatement::setString, ResultSet::getString, Types.VARCHAR, "text", ColumnTypes.StringTypes);

    /**
     * An instance that supports <code>Timestamp</code> data elements.
     */
    public static GenericColumn<Timestamp> TIMESTAMP =
            new GenericColumn<>(PreparedStatement::setTimestamp, ResultSet::getTimestamp, Types.TIMESTAMP, "timestamp", ColumnTypes.InstantTypes);

    // extension types

    /**
     * An instance that supports <code>Integer</code> or <code>int</code> data elements.
     */
    public static GenericColumn<Integer> INTEGER =
            new GenericColumn<>(PreparedStatement::setInt, ResultSet::getInt, Types.INTEGER, "integer", ColumnTypes.IntegerTypes);

    /**
     * An instance that supports <code>Byte</code> or <code>byte</code> data elements.
     */
    public static GenericColumn<Byte> BYTE =
            new GenericColumn<>(PreparedStatement::setByte, ResultSet::getByte, Types.TINYINT, "tinyint", ColumnTypes.IntegerTypes);

    /**
     * An instance that supports <code>Float</code> or <code>float</code> data elements.
     */
    public static GenericColumn<Float> FLOAT =
            new GenericColumn<>(PreparedStatement::setFloat, ResultSet::getFloat, Types.FLOAT, "float", ColumnTypes.DecimalTypes);

    /**
     * An instance that supports <code>Double</code> or <code>double</code> data elements.
     */
    public static GenericColumn<Double> DOUBLE =
            new GenericColumn<>(PreparedStatement::setDouble, ResultSet::getDouble, Types.DOUBLE, "double", ColumnTypes.DecimalTypes);

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
        if( preparedStatementSetter == null ){
            throw new NullPointerException();
        }
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
        if( preparedStatementSetter == null ){
            throw new NullPointerException();
        }
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
        if( preparedStatementSetter == null ){
            throw new NullPointerException();
        }

        this.sqlType = sqlType;
        this.preparedStatementSetter = preparedStatementSetter;
        this.resultReader = resultReader;
        this.sqlTypeName = sqlTypeName;
        this.supportedTypes = Collections.unmodifiableSet(supportedTypes);
    }

    /**
     * Create a new instance with a new SQL type name to be used in a generated
     * schema.
     *
     * @param sqlTypeName The name of this column type as represented in the
     *                    table create statement.
     * @return A new instance of the class with nothing changed except the given
     * SQL type name.
     */
    public GenericColumn<TYPE> withTypeName(String sqlTypeName){
        return new GenericColumn<>(this.preparedStatementSetter,
                this.resultReader,
                this.sqlType,
                sqlTypeName,
                this.supportedTypes);
    }

    public TYPE fromResultSet(ResultSet resultSet, String columnName) throws SQLException {
        TYPE value = resultReader.read(resultSet, columnName);
        if( resultSet.wasNull() ){
            return null;
        }
        return value;
    }

    public void setPreparedStatement(PreparedStatement preparedStatement, int index, TYPE value) throws SQLException {
        if ( value == null ) {
            preparedStatement.setNull(index,sqlType());
        }
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
