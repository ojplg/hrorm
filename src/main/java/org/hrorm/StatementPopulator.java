package org.hrorm;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Interface defining the ability to set the variables of a prepared statement.
 *
 * <p>
 *
 * Most users of hrorm will have no need to directly use this.
 */
public interface StatementPopulator {
    void populate(PreparedStatement preparedStatement) throws SQLException;

    default String render() { return ""; }

    public class Empty implements StatementPopulator {
        @Override
        public void populate(PreparedStatement preparedStatement) throws SQLException {

        }
    }

}
