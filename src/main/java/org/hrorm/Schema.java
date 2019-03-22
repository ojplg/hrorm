package org.hrorm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Schema {

    private final Map<String, DaoDescriptor> descriptorsByTableName;
    private final Set<String> sequenceNames;

    public Schema(DaoDescriptor ... descriptors){
        Map<String, DaoDescriptor> tableNameMap = new HashMap<>();
        Set<String> sequenceNames = new HashSet<>();

        for(DaoDescriptor daoDescriptor : descriptors){
            String tableName = daoDescriptor.tableName().toUpperCase();
            tableNameMap.put(tableName, daoDescriptor);

            String sequenceName = daoDescriptor.primaryKey().getSequenceName().toUpperCase();
            sequenceNames.add(sequenceName);
        }

        this.descriptorsByTableName = Collections.unmodifiableMap(tableNameMap);
        this.sequenceNames = Collections.unmodifiableSet(sequenceNames);
    }

    private String renderColumn(Column<?,?> column){
        String base =  column.getName() + " " + ColumnTypes.getSchemaColumnType(column);
        if( column.isNullable() ){
            return base;
        }
        return base + " not null";
    }

    private List<String> joinConstraints(DaoDescriptor<?,?> descriptor){
        List<String> constraints = new ArrayList<>();
        for( JoinColumn<?,?,?,?> joinColumn : descriptor.joinColumns() ) {
            StringBuilder buf = new StringBuilder();
            buf.append("alter table ");
            buf.append(descriptor.tableName());
            buf.append(" add foreign key ( ");
            buf.append(joinColumn.getName());
            buf.append(" ) references ");
            buf.append(joinColumn.getTable());
            buf.append(" ( ");
            buf.append(joinColumn.getJoinedTablePrimaryKeyName());
            buf.append(" ); ");

            constraints.add(buf.toString());
        }
        return constraints;
    }

    private List<String> childConstraints(DaoDescriptor<?,?> descriptor){

        List<String> constraints = new ArrayList<>();
        for( ChildrenDescriptor<?,?,?,?> childDescriptor : descriptor.childrenDescriptors()){
            StringBuilder buf = new StringBuilder();
            buf.append("alter table ");
            buf.append(childDescriptor.childTableName());
            buf.append(" add foreign key ( ");
            buf.append(childDescriptor.parentChildColumnName());
            buf.append(" ) references ");
            buf.append(descriptor.tableName());
            buf.append(" ( ");
            buf.append(descriptor.primaryKey().getName());
            buf.append(" ); ");

            constraints.add(buf.toString());
        }
        return constraints;
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
        List<String> columnSqls = descriptor.allColumns().stream()
                .filter(c -> ! c.isPrimaryKey())
                .map(this::renderColumn)
                .collect(Collectors.toList());

        buf.append(String.join(",\n", columnSqls));

        buf.append(");\n");

        return buf.toString();
    }

    public List<String> constraints(){
        List<String> constraints = new ArrayList<>();
        for(DaoDescriptor<?,?> descriptor : descriptorsByTableName.values()){
            constraints.addAll(joinConstraints(descriptor));
            constraints.addAll(childConstraints(descriptor));
        }
        return constraints;
    }

    public String createSequenceSql(String sequenceName){
        return "create sequence " + sequenceName + ";\n";
    }

    public String sql(){
        StringBuilder buf = new StringBuilder();
        for(String sequenceName : sequenceNames){
            buf.append(createSequenceSql(sequenceName));
            buf.append("\n");
        }

        for(String tableName : descriptorsByTableName.keySet()){
            buf.append(createTableSql(tableName));
            buf.append("\n");
        }

        for(String constraint : constraints()){
            buf.append(constraint);
            buf.append("\n");
        }

        return buf.toString();
    }


}
