package org.hrorm;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class ColumnSelection<ENTITY, BUILDER> {

    private static final ColumnSelection EMPTY = new ColumnSelection(Collections.emptyList(), new String[0]);

    private final SortedMap<String, Column<ENTITY, BUILDER>> columns;

    public ColumnSelection(List<Column<ENTITY, BUILDER>> allColumns, String ... columnNames){
        SortedMap<String, Column<ENTITY,BUILDER>> map = new TreeMap<>();
        Set<String> nameSet = Arrays.stream(columnNames)
                .map(String::toUpperCase).collect(Collectors.toSet());
        for(Column<ENTITY,BUILDER> column : allColumns){
            if (nameSet.contains(column.getName().toUpperCase())) {
                String columnNameKey = column.getName().toUpperCase();
                map.put(columnNameKey, column);
            }
        }
        this.columns = Collections.unmodifiableSortedMap(map);
    }

    public static final <E,B> ColumnSelection<E,B> empty() {
        return (ColumnSelection<E,B>) EMPTY;
    }

    public Column<ENTITY, BUILDER> get(String columnName){
        return columns.get(columnName);
    }

}
