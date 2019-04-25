package org.hrorm;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Implementers of this interface completely describe all the information
 * necessary to persisting objects of type <code>ENTITY</code>, except for
 * the primary key.
 *
 * <p>
 *     See also: {@link DaoDescriptor}
 * </p>
 *
 * Most users of hrorm will have no need to directly use this.
 *
 * @param <ENTITY> The type representing the enitity being persisted.
 * @param <ENTITYBUILDER> The type of object that can build an <code>ENTITY</code> instance.
 */
public interface KeylessDaoDescriptor<ENTITY, ENTITYBUILDER> {

    /**
     * The name of the table that is used to persist type <code>ENTITY</code>
     *
     * @return the table name
     */
    String tableName();

    /**
     * The mechanism to use to instantiate a new instance of type <code>ENTITY</code>,
     * generally a no-argument constructor of the class.
     *
     * @return A function pointer to the instantiation mechanism
     */
    Supplier<ENTITYBUILDER> supplier();

    /**
     * The mechanism for building a new entity instance.
     *
     * @return the build function
     */
    Function<ENTITYBUILDER, ENTITY> buildFunction();

    default ColumnSelection<ENTITY, ENTITYBUILDER> select(String... columnNames) {
        return new ColumnSelection(allColumns(), columnNames);
    }

    ColumnCollection<Long,ENTITY, ENTITYBUILDER> getColumnCollection();

    /**
     * All the columns in the DAO, except those that represent joins
     * to other entities, including primary key and parent column.
     *
     * @return all the data and other non-join columns
     */
    default List<Column<?, ?, ENTITY, ENTITYBUILDER>> nonJoinColumns(){
        return getColumnCollection().nonJoinColumns();
    }

    /**
     * The columns that contain the data that make up the object
     *
     * @return all the data columns supported
     */
    default List<Column<?, ?, ENTITY, ENTITYBUILDER>> dataColumns(){
        return getColumnCollection().getDataColumns();
    }

    /**
     * All the columns of the underlying table, both data type and join type.
     *
     * @return all the columns
     */
    default List<Column<?, ?, ENTITY, ENTITYBUILDER>> allColumns(){
        return getColumnCollection().allColumns();
    }

    /**
     * The columns that contain references to foreign keys to other objects
     *
     * @return all the reference columns supported
     */
    default List<JoinColumn<ENTITY, ?, ENTITYBUILDER, ?>> joinColumns(){
        return getColumnCollection().getJoinColumns();
    }

}
