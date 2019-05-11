package org.hrorm;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

/**
 * Represents a means for reading or writing a value of a column
 * on a database table for a particular entity class.
 *
 * <p>
 *     The class {@link GenericColumn} is intended for clients that
 *     wish to extend hrorm to new types.
 * </p>
 *
 * <p>
 *     Most users of hrorm will have no need to directly use this.
 * </p>
 *
 * @param <DBTYPE> The type of the data element as represented in the JDBC.
 * @param <CLASSTYPE> The type of the data element as defined in the entity class.
 * @param <ENTITY> The type of the entity.
 * @param <BUILDER> The class that is used to build new entity instances.
 */
public interface Column<DBTYPE, CLASSTYPE, ENTITY, BUILDER> {

    /**
     * The name of the database column this instance represents.
     *
     * @return The name of the column in the underlying database.
     */
    String getName();

    /**
     * The prefix that should be used for this column when generating
     * SQL. This is necessary to prevent name clashes between columns of
     * other tables that might be joined with this one.
     *
     * @return The prefix to use when generating SQL with this column instance.
     */
    String getPrefix();

    /**
     * Sets a value onto the prepared statement based on the state of the object passed.
     *
     * @param item The object to read the data from.
     * @param index Where in the prepared statement to set the read value
     * @param preparedStatement The statement being populated
     * @throws SQLException allowed for <code>PreparedStatement</code> operations
     */
    void setValue(ENTITY item, int index, PreparedStatement preparedStatement) throws SQLException;

    /**
     * Flag indicating whether or not this column is the primary key of the table
     *
     * @return true if this column represents the primary key of the table, otherwise false
     */
    default boolean isPrimaryKey(){
        return false;
    }

    /**
     * Calling this method will enforce a not-null constraint on this column.
     */
    void notNull();

    /**
     * Indicator of whether or not this column is nullable or not.
     *
     * @return true if this column is allowed to contain null values, false otherwise.
     */
    boolean isNullable();

    /**
     * Flag indicating whether this column represents a reference to a parent entity.
     *
     * @return true if it is a parent column, false otherwise
     */
    default boolean isParentColumn() {
        return false;
    }

    /**
     * Populates the passed builder object with the data read from the database.
     *
     * @param constructor The object being populated
     * @param resultSet The result said being read
     * @return A coded value indicating information about what happened
     * during population
     * @throws SQLException allowed for <code>ResultSet</code> operations
     */
    PopulateResult populate(BUILDER constructor, ResultSet resultSet) throws SQLException;


    /**
     * Make a new instance of the column, identical to this instance, except with a
     * new prefix.
     *
     * @param newPrefix The new prefix
     * @param prefixer The source for new prefixes
     * @return A new instance of the column with the reset prefix
     */
    Column<DBTYPE, CLASSTYPE, ENTITY, BUILDER> withPrefix(String newPrefix, Prefixer prefixer);

    /**
     * Applies any <code>Converter</code> associated with this column to
     * a raw database type.
     *
     * @param dbType The value of an element represented by this column, as
     *               stored in the database, or at least, as represented in the
     *               JDBC.
     * @return The value in the type as defined on the <code>ENTITY</code> class.
     */
    CLASSTYPE toClassType(DBTYPE dbType);

    /**
     * The members of <code>java.sql.Types</code> that this column should support.
     *
     * @return The types that should be handled by this column type.
     */
    default Set<Integer> supportedTypes() { return asGenericColumn().getSupportedTypes(); }

    /**
     * Returns the name of the column type in SQL, e.g. "text" for <code>String</code>,
     * "decimal" for <code>BigDecimal</code>.
     *
     * @return The SQL name of the type.
     */
    default String getSqlTypeName() { return asGenericColumn().getSqlTypeName(); }

    /**
     * Set the name of the type of this column as it would be in the SQL schema.
     *
     * @param sqlTypeName The name of the SQL type.
     */
    void setSqlTypeName(String sqlTypeName);

    /**
     * Returns a generic column instance that supports the underlying <code>DBTYPE</code>
     *
     * @return A generic column instance.
     */
    GenericColumn<DBTYPE> asGenericColumn();

    /**
     * Parses a result set object for the value of this column
     * using its name without using the prefix value.
     *
     * @param resultSet The result set to parse.
     * @return The value represented by this column.
     */
    default CLASSTYPE fromResultSet(ResultSet resultSet) {
        try {
            DBTYPE dbType = asGenericColumn().fromResultSet(resultSet, getName());
            return toClassType(dbType);
        } catch (SQLException ex){
            throw new HrormException(ex);
        }
    }

}
