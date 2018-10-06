package org.hrorm;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Representation of a single column in the table that is used
 * for persisting instances of type <code>T</code>.
 *
 * @param <T> The type of the entity being persisted.
 */
public interface TypedColumn<T> {

    String getName();
    String getPrefix();
    void populate(T item, ResultSet resultSet) throws SQLException;
    void setValue(T item, int index, PreparedStatement preparedStatement) throws SQLException;

    TypedColumn<T> withPrefix(String prefix);

    boolean isPrimaryKey();
}
