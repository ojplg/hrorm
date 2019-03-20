package org.hrorm;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Schema {

    private final Map<String, DaoDescriptor> descriptorsByTableName;
    private final Map<String, DaoDescriptor> descriptorsBySequenceName;

    public Schema(DaoDescriptor ... descriptors){
        Map<String, DaoDescriptor> tableNameMap = new HashMap<>();
        Map<String, DaoDescriptor> sequenceNameMap = new HashMap<>();

        for(DaoDescriptor daoDescriptor : descriptors){
            String tableName = daoDescriptor.tableName().toUpperCase();
            tableNameMap.put(tableName, daoDescriptor);

            String sequenceName = daoDescriptor.primaryKey().getSequenceName().toUpperCase();
            sequenceNameMap.put(sequenceName, daoDescriptor);
        }

        this.descriptorsByTableName = Collections.unmodifiableMap(tableNameMap);
        this.descriptorsBySequenceName = Collections.unmodifiableMap(sequenceNameMap);
    }

    public String createTableSql(String tableName){
        DaoDescriptor<?,?> descriptor = descriptorsByTableName.get(tableName.toUpperCase());

        StringBuilder buf = new StringBuilder();

        buf.append("create table ");
        buf.append(tableName.toUpperCase());
        buf.append(" (\n");
        if( descriptor.primaryKey() != null ){
            buf.append(descriptor.primaryKey().getName());
            buf.append(" INTEGER PRIMARY KEY,\n");
        }
        List<String> columnSqls = descriptor.allColumns().stream().filter(c -> ! c.isPrimaryKey())
                .map(c -> c.getName() + " " + ColumnTypes.getSchemaColumnType(c))
                .collect(Collectors.toList());

        buf.append(String.join(",\n", columnSqls));

        buf.append(");\n");

        return buf.toString();
    }

    public String createSequenceSql(String sequenceName){
        DaoDescriptor<?,?> descriptor = descriptorsBySequenceName.get(sequenceName.toUpperCase());
        return "create sequence " + descriptor.primaryKey().getSequenceName() + ";\n";
    }

    public String sql(){
        StringBuilder buf = new StringBuilder();
        for(String sequenceName : descriptorsBySequenceName.keySet()){
            buf.append(createSequenceSql(sequenceName));
            buf.append("\n");
        }

        for(String tableName : descriptorsByTableName.keySet()){
            buf.append(createTableSql(tableName));
            buf.append("\n");
        }

        return buf.toString();
    }
}
