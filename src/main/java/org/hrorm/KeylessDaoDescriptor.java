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
     * The columns that contain the data that make up the object
     *
     * @return all the data columns supported
     */
    List<Column<ENTITY, ENTITYBUILDER>> dataColumns();

    /**
     * The columns that contain references to foreign keys to other objects
     *
     * @return all the reference columns supported
     */
    List<JoinColumn<ENTITY,?, ENTITYBUILDER,?>> joinColumns();

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
    default List<Column<ENTITY, ENTITYBUILDER>> allColumns(){
        List<Column<ENTITY, ENTITYBUILDER>> allColumns = new ArrayList<>();
        allColumns.addAll(dataColumnsWithParent());
        allColumns.addAll(joinColumns());
        return Collections.unmodifiableList(allColumns);
    }

    static <ENTITY, ENTITYBUILDER, P, PB> List<Column<ENTITY, ENTITYBUILDER>> dataColumnsWithParent(
            List<Column<ENTITY, ENTITYBUILDER>> dataColumns, ParentColumn<ENTITY, P, ENTITYBUILDER, PB> parentColumn,
            boolean hasParent){
        List<Column<ENTITY, ENTITYBUILDER>> allColumns = new ArrayList<>(dataColumns);
        if ( hasParent ) {
            allColumns.add(parentColumn);
        }
        return Collections.unmodifiableList(allColumns);
    }

    default List<Column<ENTITY, ENTITYBUILDER>> dataColumnsWithParent(){
        return KeylessDaoDescriptor.dataColumnsWithParent(dataColumns(), parentColumn(), hasParent());
    }

    default SortedMap<String, Column<ENTITY,ENTITYBUILDER>> columnMap(String... columnNames){
        SortedMap<String, Column<ENTITY,ENTITYBUILDER>> map = new TreeMap<>();
        Set<String> nameSet = Arrays.stream(columnNames)
                .map(String::toUpperCase).collect(Collectors.toSet());
        for(Column<ENTITY,ENTITYBUILDER> column : allColumns()){
            if (nameSet.contains(column.getName().toUpperCase())) {
                String columnNameKey = column.getName().toUpperCase();
                map.put(columnNameKey, column);
            }
        }
        return Collections.unmodifiableSortedMap(map);
    }

}
