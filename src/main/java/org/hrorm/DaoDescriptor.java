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
 * necessary to persisting objects of type <code>T</code>.
 *
 * <p>
 *
 * Most users of hrorm will have no need to directly use this.
 *
 * @param <T> The type representing the enitity being persisted.
 * @param <B> The type of object that can build a <code>T</code>.
 */
public interface DaoDescriptor<T,B> {

    /**
     * The name of the table that is used to persist type <code>T</code>
     *
     * @return the table name
     */
    String tableName();

    /**
     * The mechanism to use to instantiate a new instance of type <code>T</code>,
     * generally a no-argument constructor of the class.
     *
     * @return A function pointer to the instantiation mechanism
     */
    Supplier<T> supplier();

    /**
     * The columns that contain the data that make up the object
     *
     * @return all the data columns supported
     */
    List<TypedColumn<T>> dataColumns();

    /**
     * The columns that contain references to foreign keys to other objects
     *
     * @return all the reference columns supported
     */
    List<JoinColumn<T,?>> joinColumns();

    /**
     * The primary key for objects of type <code>T</code>
     *
     * @return the primary key
     */
    PrimaryKey<T> primaryKey();

    /**
     * The definitions of any entities that are owned by type <code>T</code>
     *
     * @return all the owned entities
     */
    List<ChildrenDescriptor<T, ?>> childrenDescriptors();

    <P> ParentColumn<T, P> parentColumn();

    Function<B, T> buildFunction();

    default boolean hasParent(){
        return parentColumn() != null;
    }

    /**
     * All the columns of the underlying table, both data type and join type.
     *
     * @return all the columns
     */
    default List<TypedColumn<T>> allColumns(){
        List<TypedColumn<T>> allColumns = new ArrayList<>();
        allColumns.addAll(dataColumnsWithParent());
        allColumns.addAll(joinColumns());
        return Collections.unmodifiableList(allColumns);
    }

    default List<TypedColumn<T>> dataColumnsWithParent(){
        List<TypedColumn<T>> allColumns = new ArrayList<>();
        allColumns.addAll(dataColumns());
        if ( hasParent()) {
            allColumns.add(parentColumn());
        }
        return Collections.unmodifiableList(allColumns);
    }

    default SortedMap<String, TypedColumn<T>> columnMap(String... columnNames){
        SortedMap<String, TypedColumn<T>> map = new TreeMap<>();
        Set<String> nameSet = Arrays.asList(columnNames).stream()
                .map(String::toUpperCase).collect(Collectors.toSet());
        for(TypedColumn<T> column : allColumns()){
            if (nameSet.contains(column.getName().toUpperCase())) {
                String columnNameKey = column.getName().toUpperCase();
                map.put(columnNameKey, column);
            }
        }
        return Collections.unmodifiableSortedMap(map);
    }
}
