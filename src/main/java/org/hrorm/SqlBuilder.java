package org.hrorm;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SqlBuilder<T> {

    private final String table;
    private final List<TypedColumn<T>> dataColumns;
    private final List<JoinColumn<T, ?>> joinColumns;
    private final PrimaryKey<T> primaryKey;

    public SqlBuilder(String table, List<TypedColumn<T>> dataColumns, List<JoinColumn<T, ?>> joinColumns, PrimaryKey<T> primaryKey) {
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
        for(JoinColumn<T, ?> joinColumn : joinColumns) {
            buf.append(", ");
            buf.append(columnsAsString(
                    joinColumn.getPrefix(),
                    true,
                    joinColumn.getColumnList()
            ));
        }
        buf.append(" from ");
        buf.append(table);
        buf.append(" a");
        for(JoinColumn joinColumn : joinColumns) {
            buf.append(", ");
            buf.append(joinColumn.getTable());
            buf.append(" ");
            buf.append(joinColumn.getPrefix());
        }
        buf.append(" where 1=1 ");
        for( int idx=0; idx<joinColumns.size(); idx++ ){
            JoinColumn joinColumn = joinColumns.get(idx);
            buf.append(" and ");
            buf.append("a.");
            buf.append(joinColumn.getName());
            buf.append("=");
            buf.append(joinColumn.getPrefix());
            buf.append(".id");
        }

        return buf.toString();
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

    public String update(T item){
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
        sql.append(" where id = ");
        sql.append(primaryKey.getKey(item));
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

}
