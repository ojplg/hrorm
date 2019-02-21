package org.hrorm;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Represents a column that contains a data element, not a reference
 * to another table.
 *
 * @param <TYPE> The type of data in the column.
 * @param <ENTITY> The type of the entity.
 * @param <BUILDER> The class that is used to build new entity instances.
 */
public interface DataColumn<TYPE, ENTITY, BUILDER> extends Column<ENTITY, BUILDER> {

    /**
     * Reads the value of the type from the entity instance.
     *
     * @param entity An instance of the entity class.
     * @return The value contained in the field described by this column.
     */
    TYPE getValue(ENTITY entity);

    /**
     * Sets a value of the type of this column onto the prepared statement passed.
     *
     * @param preparedStatement The statement to populate
     * @param index The index of the variable in the prepared statement
     * @param value The value to set onto the statement
     */
    void setPreparedStatement(PreparedStatement preparedStatement, int index, TYPE value) throws SQLException;

    DataColumn<TYPE, ENTITY, BUILDER> withPrefix(String newPrefix, Prefixer prefixer);
}
