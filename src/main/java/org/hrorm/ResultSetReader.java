package org.hrorm;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface ResultSetReader<TYPE> {
    TYPE read(ResultSet resultSet, String columnName) throws SQLException;
}
