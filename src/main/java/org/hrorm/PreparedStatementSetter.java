package org.hrorm;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
interface PreparedStatementSetter<VALUE> {
    void apply(PreparedStatement preparedStatement, int index, VALUE value) throws SQLException;
}
