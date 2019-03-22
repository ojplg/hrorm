package org.hrorm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The <code>Schema</code> class can be used to generate SQL to
 * create tables, sequences, and constraints described by
 * <code>DaoDescriptor</code> objects.
 *
 * <p>
 *     The SQL generated is perhaps not ideal. It uses lowest-common
 *     denominator like "text" and "integer" and databases have more
 *     precise types that should probably be preferred.
 * </p>
 */
public class Schema {

    private final Map<String, DaoDescriptor> descriptorsByTableName;
    private final Map<String, AssociationDaoDescriptor> associationDescriptorsByTableName;
    private final Set<String> sequenceNames;

    /**
     * Construct an instance.
     *
     * @param descriptors The <code>DaoDescriptor</code> objects to generate
     *                    SQL for.
     */
    public Schema(DaoDescriptor ... descriptors){
        this(descriptors, new AssociationDaoBuilder[0]);
    }

    /**
     * Construct an instance.
     *
     * @param daoDescriptors The <code>DaoDescriptor</code> objects to generate
     *                    SQL for.
     * @param associationDescriptors The <code>AssociationDaoDescriptor</code> objects
     *                               to generate SQL for.
     */
    public Schema(DaoDescriptor[] daoDescriptors, AssociationDaoDescriptor[] associationDescriptors){
        Map<String, DaoDescriptor> tableNameMap = new HashMap<>();
        Set<String> sequenceNames = new HashSet<>();

        for(DaoDescriptor daoDescriptor : daoDescriptors){
            String tableName = daoDescriptor.tableName().toUpperCase();
            tableNameMap.put(tableName, daoDescriptor);

            String sequenceName = daoDescriptor.primaryKey().getSequenceName().toUpperCase();
            sequenceNames.add(sequenceName);
        }

        Map<String, AssociationDaoDescriptor> associationDaoBuilderMap = new HashMap<>();
        for( AssociationDaoDescriptor associationDaoDescriptor : associationDescriptors){
            String tableName = associationDaoDescriptor.getTableName().toUpperCase();
            associationDaoBuilderMap.put(tableName, associationDaoDescriptor);

            String sequenceName = associationDaoDescriptor.getSequenceName().toUpperCase();
            sequenceNames.add(sequenceName);
        }

        this.descriptorsByTableName = Collections.unmodifiableMap(tableNameMap);
        this.sequenceNames = Collections.unmodifiableSet(sequenceNames);
        this.associationDescriptorsByTableName = Collections.unmodifiableMap(associationDaoBuilderMap);
    }

    private String renderColumn(Column<?,?> column){
        String extension = column.isNullable() ? "" : " not null";
        return column.getName() + " " + ColumnTypes.getSchemaColumnType(column) + extension;
    }

    private List<String> joinConstraints(DaoDescriptor<?,?> descriptor){
        List<String> constraints = new ArrayList<>();
        for( JoinColumn<?,?,?,?> joinColumn : descriptor.joinColumns() ) {
            String constraint = foreignKeyConstraint(
                    descriptor.tableName(),
                    joinColumn.getName(),
                    joinColumn.getTable(),
                    joinColumn.getJoinedTablePrimaryKeyName()
            );
            constraints.add(constraint);
        }
        return constraints;
    }

    private List<String> childConstraints(DaoDescriptor<?,?> descriptor){
        List<String> constraints = new ArrayList<>();
        for( ChildrenDescriptor<?,?,?,?> childDescriptor : descriptor.childrenDescriptors()){
            String constraint = foreignKeyConstraint(
                    childDescriptor.childTableName(),
                    childDescriptor.parentChildColumnName(),
                    descriptor.tableName(),
                    descriptor.primaryKey().getName()
            );
            constraints.add(constraint);
        }
        return constraints;
    }

    /**
     * Generate the SQL CREATE statement for the requested table.
     *
     * @param tableName The name of the table.
     * @return the CREATE statement
     */
    public String createTableSql(String tableName){
        String upperCaseTableName = tableName.toUpperCase();

        if( descriptorsByTableName.containsKey(upperCaseTableName)){
            return createRegularTableSql(tableName);
        }

        if ( associationDescriptorsByTableName.containsKey(upperCaseTableName)){
            return createAssociationTableSql(tableName);
        }

        throw new HrormException("Do not recognize table name " + tableName);
    }

    private String createRegularTableSql(String tableName){
        DaoDescriptor<?,?> descriptor = descriptorsByTableName.get(tableName.toUpperCase());

        StringBuilder buf = new StringBuilder();

        buf.append("create table ");
        buf.append(tableName.toUpperCase());
        buf.append(" (\n");
        if( descriptor.primaryKey() != null ){
            buf.append(descriptor.primaryKey().getName());
            buf.append(" integer primary key,\n");
        }
        List<String> columnSqls = descriptor.allColumns().stream()
                .filter(c -> ! c.isPrimaryKey())
                .map(this::renderColumn)
                .collect(Collectors.toList());

        buf.append(String.join(",\n", columnSqls));

        buf.append(");\n");

        return buf.toString();
    }

    private String createAssociationTableSql(String tableName){
        AssociationDaoDescriptor descriptor = associationDescriptorsByTableName.get(tableName.toUpperCase());

        StringBuilder buf = new StringBuilder();

        buf.append("create table ");
        buf.append(tableName);
        buf.append(" (\n");
        buf.append(descriptor.getPrimaryKeyName());
        buf.append(" integer primary key,\n");
        buf.append(descriptor.getLeftColumnName());
        buf.append(" integer not null,\n");
        buf.append(descriptor.getRightColumnName());
        buf.append(" integer not null\n");
        buf.append(");");

        return buf.toString();
    }

    private List<String> associationConstraints(AssociationDaoDescriptor descriptor){
        String leftConstraint = foreignKeyConstraint(
                descriptor.getTableName(),
                descriptor.getLeftColumnName(),
                descriptor.getLeftTableName(),
                descriptor.getLeftPrimaryKeyName()
        );

        String rightConstraint = foreignKeyConstraint(
                descriptor.getTableName(),
                descriptor.getRightColumnName(),
                descriptor.getRightTableName(),
                descriptor.getRightPrimaryKeyName()
        );

        return Arrays.asList(leftConstraint, rightConstraint);
    }

    private String foreignKeyConstraint(String tableName, String columnName, String foreignTableName, String foreignPrimaryKey){
        StringBuilder buf = new StringBuilder();

        buf.append("alter table ");
        buf.append(tableName);
        buf.append(" add foreign key ");
        buf.append("(");
        buf.append(columnName);
        buf.append(") ");
        buf.append(" references ");
        buf.append(foreignTableName);
        buf.append("(");
        buf.append(foreignPrimaryKey);
        buf.append(");\n");

        return buf.toString();
    }

    /**
     * All the constraints this schema contains.
     *
     * @return The SQL for the constraints.
     */
    public List<String> constraints(){
        List<String> constraints = new ArrayList<>();
        for(DaoDescriptor<?,?> descriptor : descriptorsByTableName.values()){
            constraints.addAll(joinConstraints(descriptor));
            constraints.addAll(childConstraints(descriptor));
        }

        for( AssociationDaoDescriptor descriptor : associationDescriptorsByTableName.values() ){
            constraints.addAll(associationConstraints(descriptor));
        }

        return constraints;
    }

    private String createSequenceSql(String sequenceName){
        return "create sequence " + sequenceName + ";\n";
    }

    /**
     * The SQL to create the schema described by this object.
     *
     * @return the SQL
     */
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

        for(String tableName : associationDescriptorsByTableName.keySet()){
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
