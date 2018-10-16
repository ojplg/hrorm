package org.hrorm;

import java.sql.Connection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Complete definition of how a child entity is related to its parent entity.
 *
 * <p>
 *
 * Most users of hrorm will have no need to directly use this.
*/
public class ChildrenDescriptor<PARENT,CHILD,PARENTBUILDER,CHILDBUILDER> {

    private final String parentChildColumnName;
    private final Function<PARENT, List<CHILD>> getter;
    private final BiConsumer<PARENTBUILDER, List<CHILD>> setter;
    private final DaoDescriptor<CHILD,CHILDBUILDER> daoDescriptor;
    private final IndirectPrimaryKey<PARENT,PARENTBUILDER> parentPrimaryKey;
    private final BiConsumer<CHILDBUILDER, PARENT> parentSetter;

    private final List<ChildrenDescriptor<CHILD,?,CHILDBUILDER,?>> grandChildrenDescriptors;

    private Function<CHILDBUILDER, CHILD> childBuild;

    private final SqlBuilder<CHILD> sqlBuilder;

    public ChildrenDescriptor(Function<PARENT, List<CHILD>> getter,
                              BiConsumer<PARENTBUILDER, List<CHILD>> setter,
                              DaoDescriptor<CHILD,CHILDBUILDER> daoDescriptor,
                              IndirectPrimaryKey<PARENT,PARENTBUILDER> parentPrimaryKey) {
        this.parentChildColumnName = daoDescriptor.parentColumn().getName();
        this.getter = getter;
        this.setter = setter;
        this.daoDescriptor = daoDescriptor;
        this.parentPrimaryKey = parentPrimaryKey;

        ParentColumn<CHILD, PARENT, CHILDBUILDER, PARENTBUILDER> parentColumn = daoDescriptor.parentColumn();
        parentColumn.setParentPrimaryKey(parentPrimaryKey);
        this.parentSetter = parentColumn.setter();
        this.grandChildrenDescriptors = daoDescriptor.childrenDescriptors();

        this.sqlBuilder = new SqlBuilder<>(daoDescriptor);
    }

    public void populateChildren(Connection connection, PARENTBUILDER parentBuilder){
        SortedMap<String, TypedColumn<CHILD>> columnNameMap = daoDescriptor.columnMap(parentChildColumnName);
        CHILDBUILDER childBuilder = daoDescriptor.supplier().get();
        CHILD child = childBuild.apply(childBuilder);
        //parentSetter.accept(childBuilder, parent);
        String sql = sqlBuilder.selectByColumns(parentChildColumnName);
        SqlRunner<CHILD,CHILDBUILDER> sqlRunner = new SqlRunner<>(connection, daoDescriptor);
        List<String> parentChildColumnNameList = Collections.singletonList(parentChildColumnName);
        List<ChildrenDescriptor<CHILD,?,CHILDBUILDER, ?>> childrenDescriptorsList = daoDescriptor.childrenDescriptors();

        Supplier<CHILDBUILDER> supplier = daoDescriptor.supplier();

        List<CHILDBUILDER> childrenBuilders = sqlRunner.selectByColumns(
                sql,
                supplier,
                parentChildColumnNameList,
                columnNameMap,
                childrenDescriptorsList,
                child);

        for( CHILDBUILDER childrenBuilder : childrenBuilders ){
            for( ChildrenDescriptor<CHILD,?,CHILDBUILDER,?> grandChildDescriptor : grandChildrenDescriptors ){
                grandChildDescriptor.populateChildren(connection, childrenBuilder);
            }
        }

        List<CHILD> children = childrenBuilders.stream()
                .map (cb-> childBuild.apply(cb)).collect(Collectors.toList());

        setter.accept(parentBuilder, children);
    }

    public void saveChildren(Connection connection, PARENT item){

        SqlRunner<CHILD,CHILDBUILDER> sqlRunner = new SqlRunner<>(connection, daoDescriptor);

        List<CHILD> children = getter.apply(item);
        if( children == null ){
            children = Collections.emptyList();
        }
        Long parentId = parentPrimaryKey.getKey(item);

        Set<Long> existingIds = findExistingChildrenIds(connection, parentId);
        for(CHILD child : children){
//            parentSetter.accept(child, item);
            Long childId = daoDescriptor.primaryKey().getKey(child);
            if( childId == null ) {
                long id = DaoHelper.getNextSequenceValue(connection, daoDescriptor.primaryKey().getSequenceName());
                ((DirectPrimaryKey<CHILD>)daoDescriptor.primaryKey()).setKey(child, id);
                String sql = sqlBuilder.insert();
                sqlRunner.insert(sql, child, id);
            } else {
                existingIds.remove(childId);
                String sql = sqlBuilder.update();
                sqlRunner.update(sql, child);
            }
            for(ChildrenDescriptor<CHILD,?,?,?> grandchildrenDescriptor : grandChildrenDescriptors){
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
            for( ChildrenDescriptor<CHILD,?,?,?> grandChildDescriptor : grandChildrenDescriptors){
                Set<Long> badGranchildIds = grandChildDescriptor.findExistingChildrenIds(connection, badId);
                grandChildDescriptor.deleteOrphans(connection, badGranchildIds);
            }
            DaoHelper.runPreparedDelete(connection, preparedSql, badId);
        }
    }

}
