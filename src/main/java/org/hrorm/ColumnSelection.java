package org.hrorm;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a sub set of columns that are relevant to a particular query.
 *
 * <p>
 *
 * Most users of hrorm will have no need to directly use this.
 *
 * @param <ENTITY> The type of the entity.
 * @param <BUILDER> The class that is used to build new entity instances.
 */
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
        this.columnNames = Collections.unmodifiableList(Arrays.asList(columnNames));
    }

    public static <E,B> ColumnSelection<E,B> empty() {
        return (ColumnSelection<E,B>) EMPTY;
    }

    public Column<ENTITY, BUILDER> get(String columnName){
        return columns.get(columnName);
    }

    public StatementPopulator buildPopulator(ENTITY entity) {
        return preparedStatement -> {
            int index = 1;
            for (String columnName : columnNames) {
                Column<ENTITY, BUILDER> column = columns.get(columnName.toUpperCase());
                column.setValue(entity, index, preparedStatement);
                index++;
            }
        };
    }

    public String whereClause(){
        StringBuilder buf = new StringBuilder();

        buf.append(" where ");

        for(int idx=0; idx<columnNames.size(); idx++ ){
            String columnName = columnNames.get(idx);
            if( idx > 0 ) {
                buf.append(" and ");
            }
            buf.append("a.");
            buf.append(columnName);
            buf.append(" = ? ");
        }

        return buf.toString();
    }

}
