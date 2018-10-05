package org.hrorm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Supplier;

public interface DaoDescriptor<T> {

    String tableName();
    Supplier<T> supplier();
    List<TypedColumn<T>> dataColumns();
    List<JoinColumn<T,?>> joinColumns();
    PrimaryKey<T> primaryKey();
    List<ChildrenDescriptor<T, ?>> childrenDescriptors();

    default List<TypedColumn<T>> allColumns(){
        List<TypedColumn<T>> allColumns = new ArrayList<>();
        allColumns.addAll(dataColumns());
        allColumns.addAll(joinColumns());
        return Collections.unmodifiableList(allColumns);
    }

    default SortedMap<String, TypedColumn<T>> columnMap(String... columnNames){
        SortedMap<String, TypedColumn<T>> map = new TreeMap<>();
        HashSet<String> nameSet = new HashSet<>(Arrays.asList(columnNames));
        for(TypedColumn<T> column : allColumns()){
            if (nameSet.contains(column.getName())) {
                map.put(column.getName(), column);
            }
        }
        return Collections.unmodifiableSortedMap(map);
    }
}
