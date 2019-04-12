package org.hrorm;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * An interface representing a function that can set a value onto
 * a <code>java.sql.PreparedStatement</code>.
 *
 * <p>
 *     See: {@link GenericColumn}.
 * </p>
 *
 * @param <TYPE> The type to be set onto the <code>PreparedStatement</code>.
 */
@FunctionalInterface
interface PreparedStatementSetter<TYPE> {
    void apply(PreparedStatement preparedStatement, int index, TYPE value) throws SQLException;
}
