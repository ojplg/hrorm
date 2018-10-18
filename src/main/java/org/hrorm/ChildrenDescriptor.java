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
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Complete definition of how a child entity is related to its parent entity.
 *
 * <p>
 *
 * Most users of hrorm will have no need to directly use this.
*/
public class ChildrenDescriptor<PARENT,CHILD,PARENTBUILDER,CHILDBUILDER> {


    private static final Logger logger = Logger.getLogger("org.hrorm");

    private final Function<PARENT, List<CHILD>> getter;
    private final BiConsumer<PARENTBUILDER, List<CHILD>> setter;
    private final DaoDescriptor<CHILD,CHILDBUILDER> childDaoDescriptor;
    private final BiConsumer<CHILDBUILDER, PARENT> parentSetter;

    private final Function<CHILDBUILDER, CHILD> childBuild;
    private final Function<PARENTBUILDER, PARENT> parentBuild;

    private final SqlBuilder<CHILD> sqlBuilder;

    public ChildrenDescriptor(Function<PARENT, List<CHILD>> getter,
                              BiConsumer<PARENTBUILDER, List<CHILD>> setter,
                              DaoDescriptor<CHILD,CHILDBUILDER> childDaoDescriptor,
                              PrimaryKey<PARENT,PARENTBUILDER> parentPrimaryKey,
                              Function<CHILDBUILDER, CHILD> childBuild,
                              Function<PARENTBUILDER, PARENT> parentBuild) {
        this.getter = getter;
        this.setter = setter;
        this.childDaoDescriptor = childDaoDescriptor;

        ParentColumn<CHILD, PARENT, CHILDBUILDER, PARENTBUILDER> parentColumn = childDaoDescriptor.parentColumn();
        parentColumn.setParentPrimaryKey(parentPrimaryKey);


        this.parentSetter = parentColumn.setter();

        this.sqlBuilder = new SqlBuilder<>(childDaoDescriptor);

        this.childBuild = childBuild;
        this.parentBuild = parentBuild;
    }

    public void populateChildren(Connection connection, PARENTBUILDER parentBuilder){

        logger.info("Populating children for " + parentBuilder);
        PARENT parent = parentBuild.apply(parentBuilder);

        CHILDBUILDER childBuilder = childDaoDescriptor.supplier().get();
        parentSetter.accept(childBuilder, parent);
        CHILD child = childBuild.apply(childBuilder);

        logger.info("Instantiated a child " + child);

        SortedMap<String, Column<CHILD, CHILDBUILDER>> columnNameMap = childDaoDescriptor.columnMap(parentChildColumnName());

        String sql = sqlBuilder.selectByColumns(parentChildColumnName());
        SqlRunner<CHILD,CHILDBUILDER> sqlRunner = new SqlRunner<>(connection, childDaoDescriptor);
        List<String> parentChildColumnNameList = Collections.singletonList(parentChildColumnName());
        List<ChildrenDescriptor<CHILD,?,CHILDBUILDER, ?>> childrenDescriptorsList = childDaoDescriptor.childrenDescriptors();

        Supplier<CHILDBUILDER> supplier = childDaoDescriptor.supplier();

        List<CHILDBUILDER> childrenBuilders = sqlRunner.selectByColumns(
                sql,
                supplier,
                parentChildColumnNameList,
                columnNameMap,
                childrenDescriptorsList,
                child);

        for( CHILDBUILDER childrenBuilder : childrenBuilders ){
            for( ChildrenDescriptor<CHILD,?,CHILDBUILDER,?> grandChildDescriptor : grandChildrenDescriptors() ){
                grandChildDescriptor.populateChildren(connection, childrenBuilder);
            }
        }

        List<CHILD> children = childrenBuilders.stream()
                .map (cb-> childBuild.apply(cb)).collect(Collectors.toList());

        setter.accept(parentBuilder, children);
    }

    public void saveChildren(Connection connection, Envelope<PARENT> envelope){

        PARENT item = envelope.getItem();

        logger.info("Working to save " + item);

        SqlRunner<CHILD,CHILDBUILDER> sqlRunner = new SqlRunner<>(connection, childDaoDescriptor);

        List<CHILD> children = getter.apply(item);

        logger.info(" has children " + children);

        if( children == null ){
            children = Collections.emptyList();
        }
        Long parentId = envelope.getId();

        Set<Long> existingIds = findExistingChildrenIds(connection, parentId);
        for(CHILD child : children){
            logger.info("SAVING CHILD " + child);
//            parentSetter.accept(child, item);
            Long childId = childDaoDescriptor.primaryKey().getKey(child);
            if( childId == null ) {
                childId = DaoHelper.getNextSequenceValue(connection, childDaoDescriptor.primaryKey().getSequenceName());
                childDaoDescriptor.primaryKey().optimisticSetKey(child, childId);
                String sql = sqlBuilder.insert();
                Envelope<CHILD> childEnvelope = new Envelope<>(child, childId, parentId);
                sqlRunner.insert(sql, childEnvelope);
            } else {
                existingIds.remove(childId);
                String sql = sqlBuilder.update();
                Envelope<CHILD> childEnvelope = new Envelope<>(child, childId, parentId);
                sqlRunner.update(sql, childEnvelope);
            }
            for(ChildrenDescriptor<CHILD,?,?,?> grandchildrenDescriptor : grandChildrenDescriptors()){
                grandchildrenDescriptor.saveChildren(connection, new Envelope<>(child, childId));
            }
        }
        deleteOrphans(connection, existingIds);
    }

    public Set<Long> findExistingChildrenIds(Connection connection, Long parentId){
        String sql = sqlBuilder.selectChildIds(parentChildColumnName());
        List<Long> ids = DaoHelper.readLongs(connection, sql, parentId);
        Set<Long> idSet = new HashSet<>();
        idSet.addAll(ids);
        return idSet;
    }

    private void deleteOrphans(Connection connection, Set<Long> badChildrenIds) {
        String preparedSql = sqlBuilder.delete();

        for(Long badId : badChildrenIds) {
            for( ChildrenDescriptor<CHILD,?,?,?> grandChildDescriptor : grandChildrenDescriptors()){
                Set<Long> badGranchildIds = grandChildDescriptor.findExistingChildrenIds(connection, badId);
                grandChildDescriptor.deleteOrphans(connection, badGranchildIds);
            }
            DaoHelper.runPreparedDelete(connection, preparedSql, badId);
        }
    }

    private String parentChildColumnName(){
        return childDaoDescriptor.parentColumn().getName();
    }

    private List<ChildrenDescriptor<CHILD,?,CHILDBUILDER,?>> grandChildrenDescriptors(){
        return childDaoDescriptor.childrenDescriptors();
    }

}
