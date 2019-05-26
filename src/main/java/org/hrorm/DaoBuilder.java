package org.hrorm;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A DaoBuilder provides mechanisms for defining the relationship between
 * a Java type and the table(s) that will persist the data held in the class.
 *
 * <p>
 *     Also see {@link IndirectDaoBuilder} and {@link IndirectKeylessDaoBuilder}.
 * </p>
 *
 * @param <ENTITY> The class that the Dao will support.
 */
public class DaoBuilder<ENTITY> extends Builder<ENTITY, ENTITY, DaoBuilder<ENTITY>> implements SchemaDescriptor<ENTITY, ENTITY> {

    /**
     * Create a new DaoBuilder instance.
     *
     * @param tableName The name of the table in the database.
     * @param supplier A mechanism (generally a constructor) for creating a new instance.
     */
    public DaoBuilder(String tableName, Supplier<ENTITY> supplier){
        super(tableName, supplier, t -> t);
    }


    /**
     * Set data about the primary key of the table for this type. Hrorm demands that primary keys be
     * sequence numbers from the database. GUIDs and other constructions are not allowed. All
     * Daos must have a primary key.
     *
     * @param columnName The name of the column in the table that holds the primary key.
     * @param sequenceName The name of the sequence that will provide new keys.
     * @param getter The function to call to get the primary key value from an object instance.
     * @param setter The function to call to set the primary key value to an object instance.
     * @return This instance.
     */
    public DaoBuilder<ENTITY> withPrimaryKey(String columnName, String sequenceName, Function<ENTITY, Long> getter, BiConsumer<ENTITY, Long> setter){
        PrimaryKey<ENTITY,ENTITY> primaryKey = new DirectPrimaryKey<>(daoBuilderHelper.getPrefix(), columnName, sequenceName, getter, setter);
        columnCollection.setPrimaryKey(primaryKey);
        return this;
    }
}
