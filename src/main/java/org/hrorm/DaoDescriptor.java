package org.hrorm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Implementers of this interface completely describe all the information
 * necessary to persisting objects of type <code>ENTITY</code>.
 *
 * <p>
 *
 * Most users of hrorm will have no need to directly use this.
 *
 * @param <ENTITY> The type representing the enitity being persisted.
 * @param <ENTITYBUILDER> The type of object that can build a <code>ENTITY</code>.
 */
public interface DaoDescriptor<ENTITY, ENTITYBUILDER> {

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
     * The columns that contain the data that make up the object
     *
     * @return all the data columns supported
     */
    List<IndirectTypedColumn<ENTITY, ENTITYBUILDER>> dataColumns();

    /**
     * The columns that contain references to foreign keys to other objects
     *
     * @return all the reference columns supported
     */
    List<JoinColumn<ENTITY,?, ENTITYBUILDER,?>> joinColumns();

    /**
     * The primary key for objects of type <code>ENTITY</code>
     *
     * @return the primary key
     */
    IndirectPrimaryKey<ENTITY, ENTITYBUILDER> primaryKey();

    /**
     * The definitions of any entities that are owned by type <code>ENTITY</code>
     *
     * @return all the owned entities
     */
    List<ChildrenDescriptor<ENTITY, ?, ENTITYBUILDER, ?>> childrenDescriptors();

    <P,PB> ParentColumn<ENTITY, P, ENTITYBUILDER, PB> parentColumn();

    Function<ENTITYBUILDER, ENTITY> buildFunction();

    default boolean hasParent(){
        return parentColumn() != null;
    }

    /**
     * All the columns of the underlying table, both data type and join type.
     *
     * @return all the columns
     */
    default List<IndirectTypedColumn<ENTITY, ENTITYBUILDER>> allColumns(){
        List<IndirectTypedColumn<ENTITY, ENTITYBUILDER>> allColumns = new ArrayList<>();
        allColumns.addAll(dataColumnsWithParent());
        allColumns.addAll(joinColumns());
        return Collections.unmodifiableList(allColumns);
    }

    default List<IndirectTypedColumn<ENTITY, ENTITYBUILDER>> dataColumnsWithParent(){
        List<IndirectTypedColumn<ENTITY, ENTITYBUILDER>> allColumns = new ArrayList<>();
        allColumns.addAll(dataColumns());
        if ( hasParent()) {
            allColumns.add(parentColumn());
        }
        return Collections.unmodifiableList(allColumns);
    }

    default SortedMap<String, IndirectTypedColumn<ENTITY,ENTITYBUILDER>> columnMap(String... columnNames){
        SortedMap<String, IndirectTypedColumn<ENTITY,ENTITYBUILDER>> map = new TreeMap<>();
        Set<String> nameSet = Arrays.asList(columnNames).stream()
                .map(String::toUpperCase).collect(Collectors.toSet());
        for(IndirectTypedColumn<ENTITY,ENTITYBUILDER> column : allColumns()){
            if (nameSet.contains(column.getName().toUpperCase())) {
                String columnNameKey = column.getName().toUpperCase();
                map.put(columnNameKey, column);
            }
        }
        return Collections.unmodifiableSortedMap(map);
    }
}
