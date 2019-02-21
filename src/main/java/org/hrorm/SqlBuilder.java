package org.hrorm;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This class generates SQL strings suitable to be used in
 * {@link java.sql.PreparedStatement}s.
 *
 * <p>
 *
 * Most users of hrorm will have no need to directly use this.
 *
 * @param <ENTITY> The type of the entity being persisted.
 */
public class SqlBuilder<ENTITY> implements Queries {

    private final KeylessSqlBuilder<ENTITY> keylessSqlBuilder;
    private final PrimaryKey<ENTITY,?> primaryKey;

    public SqlBuilder(DaoDescriptor<ENTITY,?> daoDescriptor){
        this.keylessSqlBuilder = new KeylessSqlBuilder<>(daoDescriptor);
        this.primaryKey = daoDescriptor.primaryKey();
    }

    public String select(){
        return keylessSqlBuilder.select();
    }

    public String selectByColumns(String ... columnNames){
        return keylessSqlBuilder.selectByColumns(new SelectColumnList(columnNames));
    }

    public String selectChildIds(String parentColumn){

        StringBuilder buf = new StringBuilder();

        buf.append("select ");
        buf.append(primaryKey.getName());
        buf.append(" from ");
        buf.append(keylessSqlBuilder.getTable());
        buf.append(" where ");
        buf.append(parentColumn);
        buf.append(" = ?");

        return buf.toString();
    }

    public String update(){
        StringBuilder sql = new StringBuilder("update ");
        sql.append(keylessSqlBuilder.getTable());
        sql.append(" set ");
        List<String> dataColumnEntries = keylessSqlBuilder.getDataColumns().stream()
                .filter(c -> ! c.isPrimaryKey())
                .map(c -> c.getName() + "= ?")
                .collect(Collectors.toList());
        sql.append(String.join(", ", dataColumnEntries));
        for(JoinColumn joinColumn : keylessSqlBuilder.getJoinColumns()){
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
        return keylessSqlBuilder.insert();
    }

    public String delete(){
        StringBuilder buf = new StringBuilder();

        buf.append("delete from ");
        buf.append(keylessSqlBuilder.getTable());
        buf.append(" where ");
        buf.append(primaryKey.getName());
        buf.append(" = ?");

        return buf.toString();
    }

}
