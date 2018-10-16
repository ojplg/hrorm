package org.hrorm;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Representation of a single column in the table that is used
 * for persisting instances of type <code>T</code>.
 *
 * <p>
 *
 * Most users of hrorm will have no need to directly use this
 *
 * @param <T> The type of the entity being persisted.
 */
public interface TypedColumn<T> {

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

}
