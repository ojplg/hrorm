package org.hrorm;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface IndirectTypedColumn<T,CONSTRUCTOR> {
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
    void setValue(T item, int index, PreparedStatement preparedStatement) throws SQLException;

    /**
     * @return true if this column represents the primary key of the table
     */
    boolean isPrimaryKey();

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
    PopulateResult populate(CONSTRUCTOR constructor, ResultSet resultSet) throws SQLException;


    /**
     * Make a new instance of the column, identical to this instance, except with a
     * new prefix.
     *
     * @param newPrefix The new prefix
     * @param prefixer The source for new prefixes
     * @return A new instance of the column with the reset prefix
     */
    IndirectTypedColumn<T,CONSTRUCTOR> withPrefix(String newPrefix, Prefixer prefixer);

}
