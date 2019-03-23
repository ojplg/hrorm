package org.hrorm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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

    private final Set<DaoDescriptor> descriptorsSet;
    private final Set<AssociationDaoDescriptor> associationDescriptorsSet;
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
        Set<DaoDescriptor> tables = new HashSet<>();
        Set<String> sequenceNames = new HashSet<>();

        for(DaoDescriptor daoDescriptor : daoDescriptors){
            tables.add(daoDescriptor);

            String sequenceName = daoDescriptor.primaryKey().getSequenceName().toUpperCase();
            sequenceNames.add(sequenceName);
        }

        Set<AssociationDaoDescriptor> associationDaoBuilders = new HashSet<>();
        for( AssociationDaoDescriptor associationDaoDescriptor : associationDescriptors){
            associationDaoBuilders.add(associationDaoDescriptor);

            String sequenceName = associationDaoDescriptor.getSequenceName().toUpperCase();
            sequenceNames.add(sequenceName);
        }

        this.descriptorsSet = Collections.unmodifiableSet(tables);
        this.sequenceNames = Collections.unmodifiableSet(sequenceNames);
        this.associationDescriptorsSet = Collections.unmodifiableSet(associationDaoBuilders);
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

    private String createRegularTableSql(DaoDescriptor<?,?> descriptor){
        StringBuilder buf = new StringBuilder();

        buf.append("create table ");
        buf.append(descriptor.tableName());
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

    private String createAssociationTableSql(AssociationDaoDescriptor descriptor){

        StringBuilder buf = new StringBuilder();

        buf.append("create table ");
        buf.append(descriptor.getTableName());
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
     * @return The SQL to create the constraints.
     */
    public List<String> constraints(){
        List<String> constraints = new ArrayList<>();
        for(DaoDescriptor<?,?> descriptor : descriptorsSet){
            constraints.addAll(joinConstraints(descriptor));
            constraints.addAll(childConstraints(descriptor));
        }

        for( AssociationDaoDescriptor descriptor : associationDescriptorsSet ){
            constraints.addAll(associationConstraints(descriptor));
        }

        return constraints;
    }

    /**
     * All the sequences this schema contains.
     *
     * @return The SQL to create the sequences.
     */
    public List<String> sequences(){
        List<String> sequences = new ArrayList<>();
        for(String sequenceName : sequenceNames){
            sequences.add(createSequenceSql(sequenceName));
        }
        return sequences;
    }

    private String createSequenceSql(String sequenceName){
        return "create sequence " + sequenceName + ";";
    }

    /**
     * All the tables this schema contains.
     *
     * @return The SQL to create the tables.
     */
    public List<String> tables(){
        List<String> tables = new ArrayList<>();

        for(DaoDescriptor descriptor : descriptorsSet){
            tables.add(createRegularTableSql(descriptor));
        }

        for(AssociationDaoDescriptor daoDescriptor : associationDescriptorsSet){
            tables.add(createAssociationTableSql(daoDescriptor));
        }

        return tables;
    }

    /**
     * The SQL to create the schema described by this object.
     *
     * @return the SQL
     */
    public String sql(){
        String sequences = String.join("\n", sequences());
        String tables = String.join("\n", tables());
        String constraints = String.join("\n", constraints());

        return String.join("\n", Arrays.asList(sequences, tables, constraints));
    }

}
