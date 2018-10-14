package org.hrorm;

import java.sql.Connection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Complete definition of how a child entity is related to its parent entity.
 *
 * <p>
 *
 * Most users of hrorm will have no need to directly use this.
*/
public class ChildrenDescriptor<PARENT,CHILD> {

    private final String parentChildColumnName;
    private final Function<PARENT, List<CHILD>> getter;
    private final BiConsumer<PARENT, List<CHILD>> setter;
    private final DaoDescriptor<CHILD> daoDescriptor;
    private final PrimaryKey<PARENT> parentPrimaryKey;
    private final BiConsumer<CHILD, PARENT> parentSetter;

    private final List<ChildrenDescriptor<CHILD,?>> grandChildrenDescriptors;

    private final SqlBuilder<CHILD> sqlBuilder;

    public ChildrenDescriptor(Function<PARENT, List<CHILD>> getter,
                              BiConsumer<PARENT, List<CHILD>> setter,
                              DaoDescriptor<CHILD> daoDescriptor,
                              PrimaryKey<PARENT> parentPrimaryKey) {
        this.parentChildColumnName = daoDescriptor.parentColumn().getName();
        this.getter = getter;
        this.setter = setter;
        this.daoDescriptor = daoDescriptor;
        this.parentPrimaryKey = parentPrimaryKey;

        ParentColumn<CHILD, PARENT> parentColumn = daoDescriptor.parentColumn();
        parentColumn.setParentPrimaryKey(parentPrimaryKey);
        this.parentSetter = parentColumn.setter();
        this.grandChildrenDescriptors = daoDescriptor.childrenDescriptors();

        this.sqlBuilder = new SqlBuilder<>(daoDescriptor);
    }

    public void populateChildren(Connection connection, PARENT item){
        SortedMap<String, TypedColumn<CHILD>> columnNameMap = daoDescriptor.columnMap(parentChildColumnName);
        CHILD key = daoDescriptor.supplier().get();
        parentSetter.accept(key, item);
        String sql = sqlBuilder.selectByColumns(parentChildColumnName);
        SqlRunner<CHILD> sqlRunner = new SqlRunner<>(connection, daoDescriptor);
        List<CHILD> children = sqlRunner.selectByColumns(sql, daoDescriptor.supplier(),
                Collections.singletonList(parentChildColumnName), columnNameMap, daoDescriptor.childrenDescriptors(), key);

        for( CHILD child : children ){
            for( ChildrenDescriptor<CHILD,?> grandChildDescriptor : grandChildrenDescriptors ){
                grandChildDescriptor.populateChildren(connection, child);
            }
        }

        setter.accept(item, children);
    }

    public void saveChildren(Connection connection, PARENT item){

        SqlRunner<CHILD> sqlRunner = new SqlRunner<>(connection, daoDescriptor);

        List<CHILD> children = getter.apply(item);
        if( children == null ){
            children = Collections.emptyList();
        }
        Long parentId = parentPrimaryKey.getKey(item);

        Set<Long> existingIds = findExistingChildrenIds(connection, parentId);
        for(CHILD child : children){
            parentSetter.accept(child, item);
            Long childId = daoDescriptor.primaryKey().getKey(child);
            if( childId == null ) {
                long id = DaoHelper.getNextSequenceValue(connection, daoDescriptor.primaryKey().getSequenceName());
                daoDescriptor.primaryKey().setKey(child, id);
                String sql = sqlBuilder.insert();
                sqlRunner.insert(sql, child);
            } else {
                existingIds.remove(childId);
                String sql = sqlBuilder.update();
                sqlRunner.update(sql, child);
            }
            for(ChildrenDescriptor<CHILD,?> grandchildrenDescriptor : grandChildrenDescriptors){
                grandchildrenDescriptor.saveChildren(connection, child);
            }
        }
        deleteOrphans(connection, existingIds);
    }

    public Set<Long> findExistingChildrenIds(Connection connection, Long parentId){
        String sql = sqlBuilder.selectChildIds(parentChildColumnName);
        List<Long> ids = DaoHelper.readLongs(connection, sql, parentId);
        Set<Long> idSet = new HashSet<>();
        idSet.addAll(ids);
        return idSet;
    }

    private void deleteOrphans(Connection connection, Set<Long> badChildrenIds) {
        String preparedSql = sqlBuilder.delete();

        for(Long badId : badChildrenIds) {
            for( ChildrenDescriptor<CHILD,?> grandChildDescriptor : grandChildrenDescriptors){
                Set<Long> badGranchildIds = grandChildDescriptor.findExistingChildrenIds(connection, badId);
                grandChildDescriptor.deleteOrphans(connection, badGranchildIds);
            }
            DaoHelper.runPreparedDelete(connection, preparedSql, badId);
        }
    }

}
