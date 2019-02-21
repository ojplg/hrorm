package org.hrorm;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface StatementPopulator {
    void populate(PreparedStatement preparedStatement) throws SQLException;
}
