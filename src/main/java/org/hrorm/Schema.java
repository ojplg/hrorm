package org.hrorm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private final List<DaoDescriptor> descriptors;
    private final List<KeylessDaoDescriptor> keylessDescriptors;

    /**
     * Construct an instance.
     *
     * @param descriptors The <code>DaoDescriptor</code> objects to generate SQL for.
     */
    public Schema(KeylessDaoDescriptor ... descriptors){
        List<DaoDescriptor> daoDescriptors = new ArrayList<>();
        List<KeylessDaoDescriptor> keylessDaoDescriptors = new ArrayList<>();

        for(KeylessDaoDescriptor descriptor : descriptors){
            if( descriptor instanceof DaoDescriptor){
                daoDescriptors.add((DaoDescriptor) descriptor);
            } else {
                keylessDaoDescriptors.add(descriptor);
            }
        }

        this.descriptors = Collections.unmodifiableList(daoDescriptors);
        this.keylessDescriptors = Collections.unmodifiableList(keylessDaoDescriptors);
    }

    private String renderColumn(Column<?,?> column){
        String extension = column.isNullable() ? "" : " not null";
        return column.getName() + " " + column.getSqlType() + extension;
    }

    private Stream<String> joinConstraints(KeylessDaoDescriptor<?,?> descriptor){
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
        return constraints.stream();
    }

    private Stream<String> childConstraints(DaoDescriptor<?,?> descriptor){
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
        return constraints.stream();
    }

    private Stream<String> allConstraints(DaoDescriptor<?,?> descriptor){
        return Stream.concat(joinConstraints(descriptor), childConstraints(descriptor));
    }

    private String tablesSql(DaoDescriptor<?,?> descriptor){
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

    private String tablesSql(KeylessDaoDescriptor<?,?> descriptor){
        StringBuilder buf = new StringBuilder();

        buf.append("create table ");
        buf.append(descriptor.tableName());
        buf.append(" (\n");
        List<String> columnSqls = descriptor.allColumns().stream()
                .filter(c -> ! c.isPrimaryKey())
                .map(this::renderColumn)
                .collect(Collectors.toList());

        buf.append(String.join(",\n", columnSqls));

        buf.append(");\n");

        return buf.toString();
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

        return Stream.concat(
                descriptors.stream().flatMap(this::allConstraints),
                keylessDescriptors.stream().flatMap(this::joinConstraints))
                .collect(Collectors.toList());
    }

    /**
     * All the sequences this schema contains.
     *
     * @return The SQL to create the sequences.
     */
    public List<String> sequences(){
        return descriptors.stream()
                .map(d -> createSequenceSql(d.primaryKey().getSequenceName()))
                .collect(Collectors.toList());
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
        return Stream.concat(
                descriptors.stream().map(this::tablesSql),
                keylessDescriptors.stream().map(this::tablesSql))
                .collect(Collectors.toList());
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
