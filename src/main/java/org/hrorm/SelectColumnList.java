package org.hrorm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SelectColumnList implements Iterable<SelectColumnList.ColumnOperatorEntry> {

    public static class ColumnOperatorEntry {
        String rawName;
        Operator operator;

        ColumnOperatorEntry(String rawName, Operator operator) {
            this.rawName = rawName;
            this.operator = operator;
        }

        public String getSqlString() {
            return operator.getSqlString(rawName);
        }
    }

    public static final SelectColumnList EMPTY = new SelectColumnList();

    private List<ColumnOperatorEntry> entries = new ArrayList<>();

    public SelectColumnList(Map<String, Operator> columnNameOperatorMap){
        for(Map.Entry<String, Operator> mapEntry : columnNameOperatorMap.entrySet()){
            ColumnOperatorEntry entry = new ColumnOperatorEntry(mapEntry.getKey(), mapEntry.getValue());
            entries.add(entry);
        }
    }

    public SelectColumnList(String ... columnNames){
        for (String columnName: columnNames) {
            ColumnOperatorEntry entry = new ColumnOperatorEntry(columnName, Operator.EQUALS);
            entries.add(entry);
        }
    }

    @Override
    public Iterator<ColumnOperatorEntry> iterator() {
        return entries.iterator();
    }

    public String[] columnNames(){
        return entries.stream().map(e -> e.rawName).collect(Collectors.toList()).toArray(new String[entries.size()]);
    }
}
