package org.hrorm;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface IndirectTypedColumn<T,CONSTRUCTOR> extends TypedColumn<T> {
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
