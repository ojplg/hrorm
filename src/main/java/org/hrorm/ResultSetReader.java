package org.hrorm;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * An interface representing a function that can read a value out
 * of a <code>java.sql.ResultSet</code>.
 *
 * <p>
 *     See: {@link GenericColumn}.
 * </p>
 *
 * @param <TYPE> The type to be read from the <code>ResultSet</code>.
 */
@FunctionalInterface
public interface ResultSetReader<TYPE> {
    TYPE read(ResultSet resultSet, String columnName) throws SQLException;
}
