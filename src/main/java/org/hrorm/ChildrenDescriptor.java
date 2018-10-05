package org.hrorm;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ChildrenDescriptor<PARENT,CHILD> {

    private final String parentChildColumnName;
    private final Function<PARENT, List<CHILD>> getter;
    private final BiConsumer<PARENT, List<CHILD>> setter;
    private final DaoDescriptor<CHILD> daoDescriptor;
    private final PrimaryKey<PARENT> primaryKey;
    private final BiConsumer<CHILD, Long> parentSetter;

    private final SqlBuilder<CHILD> sqlBuilder;

    public ChildrenDescriptor(String parentChildColumnName,
                              BiConsumer<CHILD, Long> parentSetter,
                              Function<PARENT, List<CHILD>> getter,
                              BiConsumer<PARENT, List<CHILD>> setter,
                              DaoDescriptor<CHILD> daoDescriptor,
                              PrimaryKey<PARENT> primaryKey) {
        this.parentChildColumnName = parentChildColumnName;
        this.getter = getter;
        this.setter = setter;
        this.daoDescriptor = daoDescriptor;
        this.primaryKey = primaryKey;
        this.parentSetter = parentSetter;

        this.sqlBuilder = new SqlBuilder<>(daoDescriptor.tableName(),
                daoDescriptor.dataColumns(),
                daoDescriptor.joinColumns(),
                daoDescriptor.primaryKey());
    }

    public void populateChildren(Connection connection, PARENT item){
        SortedMap<String, TypedColumn<CHILD>> columnNameMap = daoDescriptor.columnMap(parentChildColumnName);
        CHILD key = daoDescriptor.supplier().get();
        Long id = primaryKey.getKey(item);
        parentSetter.accept(key, id);
        String sql = sqlBuilder.selectByColumns(parentChildColumnName);
        SqlRunner<CHILD> sqlRunner = new SqlRunner<>(connection, daoDescriptor.dataColumns(), daoDescriptor.joinColumns());
        List<CHILD> children = sqlRunner.selectByColumns(sql, daoDescriptor.supplier(),
                Collections.singletonList(parentChildColumnName), columnNameMap, daoDescriptor.childrenDescriptors(), key);
        setter.accept(item, children);
    }

    public void saveChildren(Connection connection, PARENT item){
        SqlRunner<CHILD> sqlRunner = new SqlRunner<>(connection, daoDescriptor.dataColumns(), daoDescriptor.joinColumns());

        List<CHILD> children = getter.apply(item);
        List<Long> goodChildrenIds = new ArrayList<>();
        Long parentId = primaryKey.getKey(item);

        for(CHILD child : children){
            parentSetter.accept(child, parentId);
            if( daoDescriptor.primaryKey().getKey(child) == null ) {
                long id = DaoHelper.getNextSequenceValue(connection, daoDescriptor.primaryKey().getSequenceName());
                daoDescriptor.primaryKey().setKey(child, id);
                String sql = sqlBuilder.insert();
                sqlRunner.insert(sql, child);
                goodChildrenIds.add(id);
            } else {
                String sql = sqlBuilder.update(child);
                sqlRunner.update(sql, child);
                goodChildrenIds.add(daoDescriptor.primaryKey().getKey(child));
            }
        }
        deleteOrphans(connection, item, goodChildrenIds);
    }

    private void deleteOrphans(Connection connection, PARENT item, List<Long> goodChildrenIds) {

        StringBuilder buf = new StringBuilder();
        buf.append("delete from ");
        buf.append(daoDescriptor.tableName());
        buf.append(" where ");
        buf.append(parentChildColumnName);
        buf.append(" = ");
        buf.append(primaryKey.getKey(item));

        if( goodChildrenIds.size() > 0 ) {
            List<String> goodChildrenIdStrings = goodChildrenIds.stream().map(Object::toString).collect(Collectors.toList());

            buf.append(" and ");
            buf.append(daoDescriptor.primaryKey().getName());
            buf.append(" not in ");
            buf.append("(");
            buf.append(String.join(", ", goodChildrenIdStrings));
            buf.append(")");
        }

        String sql = buf.toString();

        DaoHelper.runDelete(connection, sql);
    }

}
