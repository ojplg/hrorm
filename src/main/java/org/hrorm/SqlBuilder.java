package org.hrorm;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class generates SQL strings suitable to be used in
 * {@link java.sql.PreparedStatement}s.
 *
 * <p>
 *
 * Most users of hrorm will have no need to directly use this.
 *
 * @param <T> The type of the entity being persisted.
 */
public class SqlBuilder<T> {

    private final String table;
    private final List<? extends TypedColumn<T>> dataColumns;
    private final List<? extends JoinColumn<T, ?, ?, ?>> joinColumns;
    private final IndirectPrimaryKey<T,?> primaryKey;

    public SqlBuilder(DaoDescriptor<T,?> daoDescriptor){
        this.table = daoDescriptor.tableName();
        this.dataColumns = daoDescriptor.dataColumnsWithParent();
        this.joinColumns = daoDescriptor.joinColumns();
        this.primaryKey = daoDescriptor.primaryKey();
    }

    public SqlBuilder(String table,
                      List<? extends TypedColumn<T>> dataColumns,
                      List<? extends JoinColumn<T, ?, ?, ?>> joinColumns,
                      IndirectPrimaryKey<T, ?> primaryKey) {
        this.table = table;
        this.dataColumns = dataColumns;
        this.joinColumns = joinColumns;
        this.primaryKey = primaryKey;
    }

    private String columnsAsString(String prefix, boolean withAliases, List<? extends TypedColumn> columns){
        Function<TypedColumn,String> stringer;
        if( withAliases && prefix != null ) {
            stringer = c -> prefix + "." + c.getName() + " as " + prefix + c.getName();
        } else if (prefix != null ){
            stringer = c -> prefix + c.getName();
        } else {
            stringer = TypedColumn::getName;
        }
        List<String> columnNames = columns.stream().map(stringer).collect(Collectors.toList());
        return String.join(", ", columnNames);
    }

    public String select(){
        StringBuilder buf = new StringBuilder();
        buf.append("select ");
        buf.append(columnsAsString("a", true, dataColumns));
        for(JoinColumn<?, ?, ?, ?> joinColumn : flattenedJoinColumns()) {
            buf.append(", ");
            buf.append(columnsAsString(
                    joinColumn.getPrefix(),
                    true,
                    joinColumn.getDataColumns()
            ));
        }
        buf.append(" from ");
        buf.append(table);
        buf.append(" a");
        List<JoinColumn> flattenedJoinColumns = flattenedJoinColumns();
        for(JoinColumn joinColumn : flattenedJoinColumns) {
            buf.append(" LEFT JOIN ");
            buf.append(joinColumn.getTable());
            buf.append(" ");
            buf.append(joinColumn.getPrefix());
            buf.append(" ON ");
            buf.append(joinColumn.getJoinedTablePrefix());
            buf.append(".");
            buf.append(joinColumn.getName());
            buf.append("=");
            buf.append(joinColumn.getPrefix());
            buf.append(".");
            buf.append(joinColumn.getJoinedTablePrimaryKeyName());
        }
        buf.append(" where 1=1 ");

        return buf.toString();
    }

    private List<JoinColumn> flattenedJoinColumns(){
        List<JoinColumn> flatJoinColumnList = new ArrayList<>();
        for(JoinColumn joinColumn : joinColumns){
            prependColumnsRecursively(flatJoinColumnList, joinColumn);
        }
        return flatJoinColumnList;
    }

    private void prependColumnsRecursively(List<JoinColumn> listToBuild, JoinColumn columnToAdd){
        List<JoinColumn> listToAppend = columnToAdd.getTransitiveJoins();
        listToAppend.forEach(c -> prependColumnsRecursively(listToBuild, c));
        listToBuild.add(0, columnToAdd);
    }

    public String selectByColumns(String ... columnNames){
        StringBuilder buf = new StringBuilder();
        buf.append(select());
        for(String columnName : columnNames){
            buf.append(" and ");
            buf.append("a.");
            buf.append(columnName);
            buf.append(" = ? ");
        }

        return buf.toString();
    }

    public String selectChildIds(String parentColumn){
        StringBuilder buf = new StringBuilder();

        buf.append("select ");
        buf.append(primaryKey.getName());
        buf.append(" from ");
        buf.append(table);
        buf.append(" where ");
        buf.append(parentColumn);
        buf.append(" = ?");

        return buf.toString();
    }

    public String update(){
        StringBuilder sql = new StringBuilder("update ");
        sql.append(table);
        sql.append(" set ");
        List<String> dataColumnEntries = dataColumns.stream()
                .filter(c -> ! c.isPrimaryKey())
                .map(c -> c.getName() + "= ?")
                .collect(Collectors.toList());
        sql.append(String.join(", ", dataColumnEntries));
        for(int idx=0; idx<joinColumns.size(); idx++){
            JoinColumn joinColumn = joinColumns.get(idx);
            sql.append(", ");
            sql.append(joinColumn.getName());
            sql.append(" = ? ");
        }
        sql.append(" where ");
        sql.append(primaryKey.getName());
        sql.append( " = ?");
        return sql.toString();
    }

    public String insert(){
        StringBuilder bldr = new StringBuilder();
        bldr.append("insert into ");
        bldr.append(table);
        bldr.append(" ( ");
        bldr.append(columnsAsString("", false, dataColumns));
        if( ! joinColumns.isEmpty() ) {
            bldr.append(", ");
            bldr.append(columnsAsString("", false, joinColumns));
        }
        bldr.append(" ) values ( ");
        int end = dataColumns.size() - 1;
        if (!joinColumns.isEmpty()){
            end += joinColumns.size();
        }
        for(int idx=0; idx<end; idx++){
            bldr.append("?, ");
        }
        bldr.append("? ");
        bldr.append(" ) ");

        return bldr.toString();
    }

    public String delete(){
        StringBuilder buf = new StringBuilder();

        buf.append("delete from ");
        buf.append(table);
        buf.append(" where ");
        buf.append(primaryKey.getName());
        buf.append(" = ?");

        return buf.toString();
    }

}
