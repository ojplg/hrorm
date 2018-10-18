package org.hrorm;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Represents a column in a table that holds entity values.
 *
 * @param <ENTITY> The type of the entity.
 * @param <BUILDER> The class that is used to build new entity instances.
 */
public interface Column<ENTITY, BUILDER> {
    /**
     * @return The name of the column in the underlying database.
     */
    String getName();

    /**
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

    default boolean isParentColumn() {
        return false;
    }

    /**
     * Populates the object with the data read from the database.
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
    Column<ENTITY, BUILDER> withPrefix(String newPrefix, Prefixer prefixer);

}
