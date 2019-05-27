package org.hrorm;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;


/**
 * An <code>IndirectDaoBuilder</code> is used for times when the class representing
 * the persisted entity is immutable. It allows the relationships between the database
 * table, the entity class, and the entity's builder class to be defined.
 *
 * <p>
 *     Also see {@link DaoBuilder} and {@link IndirectKeylessDaoBuilder}.
 * </p>
 *
 * @param <ENTITY> The type of the class that the <code>Dao</code> will support.
 * @param <BUILDER> The type of the class that can be used to construct new <code>ENTITY</code>
 *                 instances and accept individual data elements.
 */
public class IndirectDaoBuilder<ENTITY, BUILDER>
        extends AbstractDaoBuilder<ENTITY, BUILDER, IndirectDaoBuilder<ENTITY, BUILDER>>
        implements SchemaDescriptor<ENTITY, BUILDER> {

    /**
     * Create a new <code>IndirectDaoBuilder</code> instance.
     *
     * @param tableName The name of the table in the database.
     * @param supplier A mechanism (generally a constructor) for creating a new instance.
     * @param buildFunction How to create an instance of the entity class from its builder.
     */
    public IndirectDaoBuilder(String tableName, Supplier<BUILDER> supplier, Function<BUILDER, ENTITY> buildFunction){
        super(tableName, supplier, buildFunction);
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
    public IndirectDaoBuilder<ENTITY, BUILDER> withPrimaryKey(String columnName, String sequenceName, Function<ENTITY, Long> getter, BiConsumer<BUILDER, Long> setter){
        PrimaryKey<ENTITY, BUILDER> key = new IndirectPrimaryKey<>(daoBuilderHelper.getPrefix(), columnName, sequenceName, getter, setter);
        columnCollection.setPrimaryKey(key);
        return this;
    }

}
