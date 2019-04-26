package org.hrorm;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Complete definition of how a child entity is related to its parent entity.
 *
 * <p>
 *
 * Most users of hrorm will have no need to directly use this.
 */
public class ChildrenDescriptor<PARENT,CHILD,PARENTBUILDER,CHILDBUILDER,PARENTPK> {

    private final Function<PARENT, List<CHILD>> getter;
    private final BiConsumer<PARENTBUILDER, List<CHILD>> setter;
    private final DaoDescriptor<Long, CHILD,CHILDBUILDER> childDaoDescriptor;
    private final BiConsumer<CHILDBUILDER, PARENT> parentSetter;

    private final Function<PARENTBUILDER, PARENT> parentBuildFunction;

    private final SqlBuilder<CHILD> sqlBuilder;

    private final PrimaryKey<PARENTPK, PARENT, PARENTBUILDER> parentPrimaryKey;

    public ChildrenDescriptor(Function<PARENT, List<CHILD>> getter,
                              BiConsumer<PARENTBUILDER, List<CHILD>> setter,
                              DaoDescriptor<Long, CHILD,CHILDBUILDER> childDaoDescriptor,
                              PrimaryKey<PARENTPK,PARENT, PARENTBUILDER> parentPrimaryKey,
                              Function<PARENTBUILDER, PARENT> parentBuildFunction) {
        this.getter = getter;
        this.setter = setter;
        this.childDaoDescriptor = childDaoDescriptor;
        this.sqlBuilder = new SqlBuilder<>(childDaoDescriptor);
        this.parentBuildFunction = parentBuildFunction;

        ParentColumn<CHILD, PARENT, CHILDBUILDER, PARENTBUILDER, PARENTPK> parentColumn = childDaoDescriptor.parentColumn();
        parentColumn.setParentPrimaryKey(parentPrimaryKey);
        this.parentSetter = parentColumn.setter();
        this.parentPrimaryKey = parentPrimaryKey;
    }

    public void populateChildren(Connection connection, PARENTBUILDER parentBuilder){

        PARENT parent = parentBuildFunction.apply(parentBuilder);
        PARENTPK parentId = parentPrimaryKey.getKey(parent);
        int parentKeySqlType = parentPrimaryKey.supportedTypes().iterator().next();
        GenericColumn<PARENTPK> genericColumn = new GenericColumn<>(
                parentPrimaryKey.getStatementSetter(),
                parentPrimaryKey.getReader(),
                parentKeySqlType
        );

        Where where = new Where(parentChildColumnName(), Operator.EQUALS, parentId, genericColumn);

        String sql = sqlBuilder.select(where);
        SqlRunner<Long, CHILD,CHILDBUILDER> sqlRunner = new SqlRunner<>(connection, childDaoDescriptor);
        List<ChildrenDescriptor<CHILD,?,CHILDBUILDER, ?, ?>> childrenDescriptorsList = childDaoDescriptor.childrenDescriptors();

        Supplier<CHILDBUILDER> supplier = childDaoDescriptor.supplier();

        List<CHILDBUILDER> childrenBuilders = sqlRunner.selectWhere(
                sql,
                supplier,
                childrenDescriptorsList,
                where);

        List<CHILD> children = new ArrayList<>();
        for( CHILDBUILDER childrenBuilder : childrenBuilders ){
            for( ChildrenDescriptor<CHILD,?,CHILDBUILDER,?,?> grandChildDescriptor : grandChildrenDescriptors() ){
                grandChildDescriptor.populateChildren(connection, childrenBuilder);
            }
            parentSetter.accept(childrenBuilder, parent);
            CHILD c = childBuilder().apply(childrenBuilder);
            children.add(c);
        }

        setter.accept(parentBuilder, children);
    }

    public void saveChildren(Connection connection, Envelope<PARENT,Long> envelope) {

        PrimaryKey<Long, CHILD, CHILDBUILDER> childPrimaryKey = childDaoDescriptor.primaryKey();

        PARENT item = envelope.getItem();

        SqlRunner<Long, CHILD,CHILDBUILDER> sqlRunner = new SqlRunner<>(connection, childDaoDescriptor);

        List<CHILD> children = getter.apply(item);

        if( children == null ){
            children = Collections.emptyList();
        }
        Long parentId = envelope.getId();

        Set<Long> existingIds = findExistingChildrenIds(connection, parentId);

        for(CHILD child : children){
            Long childId = childPrimaryKey.getKey(child);
            if( childId == null ) {
                KeyProducer<Long> keyProducer = childPrimaryKey.getKeyProducer();
                childId = keyProducer.produceKey(connection);
                childPrimaryKey.optimisticSetKey(child, childId);
                String sql = sqlBuilder.insert();
                Envelope<CHILD, Long> childEnvelope = new Envelope<>(child, childId, parentId);
                sqlRunner.insert(sql, childEnvelope);
            } else {
                existingIds.remove(childId);
                String sql = sqlBuilder.update();
                Envelope<CHILD, Long> childEnvelope = new Envelope<>(child, childId, parentId);
                sqlRunner.update(sql, childEnvelope);
            }
            for(ChildrenDescriptor<CHILD,?,?,?,?> grandchildrenDescriptor : grandChildrenDescriptors()){
                grandchildrenDescriptor.saveChildren(connection, new Envelope<>(child, childId));
            }
        }
        deleteOrphans(connection, existingIds);

    }

    private Set<Long> findExistingChildrenIds(Connection connection, Long parentId){
        String sql = sqlBuilder.selectChildIds(parentChildColumnName());
        SqlRunner<Long, PARENT,PARENTBUILDER> sqlRunner = new SqlRunner(connection);
        return sqlRunner.runChildSelectChildIds(sql, parentId);
    }

    private void deleteOrphans(Connection connection, Set<Long> badChildrenIds) {
        String preparedSql = sqlBuilder.delete();
        SqlRunner<Long, CHILD, CHILDBUILDER> sqlRunner = new SqlRunner<>(connection);

        for(Long badId : badChildrenIds) {
            for( ChildrenDescriptor<CHILD,?,?,?,?> grandChildDescriptor : grandChildrenDescriptors()){
                Set<Long> badGranchildIds = grandChildDescriptor.findExistingChildrenIds(connection, badId);
                grandChildDescriptor.deleteOrphans(connection, badGranchildIds);
            }
            sqlRunner.runPreparedDelete(preparedSql, badId);
        }
    }

    public String parentChildColumnName(){
        return childDaoDescriptor.parentColumn().getName();
    }

    public String childTableName() { return childDaoDescriptor.tableName(); }

    private List<ChildrenDescriptor<CHILD,?,CHILDBUILDER,?,?>> grandChildrenDescriptors(){
        return childDaoDescriptor.childrenDescriptors();
    }

    private Function<CHILDBUILDER, CHILD> childBuilder(){
        return childDaoDescriptor.buildFunction();
    }

    @Override
    public String toString() {
        return "ChildrenDescriptor for " + childDaoDescriptor.tableName();
    }
}
