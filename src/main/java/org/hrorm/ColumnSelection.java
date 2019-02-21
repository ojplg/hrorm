package org.hrorm;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ColumnSelection<ENTITY, BUILDER> {

    private static final ColumnSelection EMPTY = new ColumnSelection(Collections.emptyList());

    private final Map<String, Column<ENTITY, BUILDER>> columns;
    private final List<String> columnNames;

    public ColumnSelection(List<Column<ENTITY, BUILDER>> allColumns, String ... columnNames){
        Map<String, Column<ENTITY,BUILDER>> map = new HashMap<>();
        Set<String> nameSet = Arrays.stream(columnNames)
                .map(String::toUpperCase).collect(Collectors.toSet());
        for(Column<ENTITY,BUILDER> column : allColumns){
            if (nameSet.contains(column.getName().toUpperCase())) {
                String columnNameKey = column.getName().toUpperCase();
                map.put(columnNameKey, column);
            }
        }
        this.columns = Collections.unmodifiableMap(map);
        this.columnNames = Arrays.asList(columnNames);
    }

    public static final <E,B> ColumnSelection<E,B> empty() {
        return (ColumnSelection<E,B>) EMPTY;
    }

    public Column<ENTITY, BUILDER> get(String columnName){
        return columns.get(columnName);
    }

    public StatementPopulator buildPopulator(ENTITY entity) {
        return new StatementPopulator() {
            @Override
            public void populate(PreparedStatement preparedStatement) throws SQLException {
                int index = 1;
                for (String columnName : columnNames) {
                    Column<ENTITY, BUILDER> column = columns.get(columnName.toUpperCase());
                    column.setValue(entity, index, preparedStatement);
                    index++;
                }
            }
        };
    }
}
