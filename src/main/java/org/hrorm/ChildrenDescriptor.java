package org.hrorm;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Complete definition of how a child entity is related to its parent entity.
 *
 * <br>
 *
 * Most users of hrorm will have no need to directly use this.
 */
public class ChildrenDescriptor<PARENT,CHILD,PARENTBUILDER,CHILDBUILDER> {

    private static final Logger logger = Logger.getLogger("org.hrorm");

    private final Function<PARENT, List<CHILD>> getter;
    private final BiConsumer<PARENTBUILDER, List<CHILD>> setter;
    private final DaoDescriptor<CHILD,CHILDBUILDER> childDaoDescriptor;
    private final BiConsumer<CHILDBUILDER, PARENT> parentSetter;

    private final Function<PARENTBUILDER, PARENT> parentBuildFunction;

    private final SqlBuilder<CHILD> sqlBuilder;

    private final PrimaryKey<PARENT, PARENTBUILDER> parentPrimaryKey;

    public ChildrenDescriptor(Function<PARENT, List<CHILD>> getter,
                              BiConsumer<PARENTBUILDER, List<CHILD>> setter,
                              DaoDescriptor<CHILD,CHILDBUILDER> childDaoDescriptor,
                              PrimaryKey<PARENT, PARENTBUILDER> parentPrimaryKey,
                              Function<PARENTBUILDER, PARENT> parentBuildFunction) {
        this.getter = getter;
        this.setter = setter;
        this.childDaoDescriptor = childDaoDescriptor;
        this.sqlBuilder = new SqlBuilder<>(childDaoDescriptor);
        this.parentBuildFunction = parentBuildFunction;

        ParentColumn<CHILD, PARENT, CHILDBUILDER, PARENTBUILDER> parentColumn = childDaoDescriptor.parentColumn();
        parentColumn.setParentPrimaryKey(parentPrimaryKey);
        this.parentSetter = parentColumn.setter();
        this.parentPrimaryKey = parentPrimaryKey;
    }

    /*
     * This method will search for and build the children of the passed parent builder,
     * setting them onto the parent builder.
     */
    public void populateChildren(Connection connection, PARENTBUILDER parentBuilder){

        // generate sql for the selection of children
        PARENT parent = parentBuildFunction.apply(parentBuilder);
        long parentId = parentPrimaryKey.getKey(parent);
        Where where = new Where(parentChildColumnName(), Operator.EQUALS, parentId);
        String sql = sqlBuilder.select(where);

        // Run the SQL, passing in children of this child
        SqlRunner<CHILD,CHILDBUILDER> sqlRunner = new SqlRunner<>(connection, childDaoDescriptor);
        List<ChildrenDescriptor<CHILD,?,CHILDBUILDER, ?>> childrenDescriptorsList = childDaoDescriptor.childrenDescriptors();
        Supplier<CHILDBUILDER> supplier = childDaoDescriptor.supplier();
        List<CHILDBUILDER> childrenBuilders = sqlRunner.selectWhereStandard(
                sql,
                supplier,
                childrenDescriptorsList,
                where);

        // build the child objects
        List<CHILD> children = new ArrayList<>();
        for( CHILDBUILDER childrenBuilder : childrenBuilders ){
            for( ChildrenDescriptor<CHILD,?,CHILDBUILDER,?> grandChildDescriptor : grandChildrenDescriptors() ){
                grandChildDescriptor.populateChildren(connection, childrenBuilder);
            }
            parentSetter.accept(childrenBuilder, parent);
            CHILD c = childBuilder().apply(childrenBuilder);
            children.add(c);
        }

        // set them onto the parent
        setter.accept(parentBuilder, children);

        logger.warning("and set " + children.size());
    }

    /*
     * Bulk populator of children for a collection of parents. Since there are several
     * ways to select all the children of all the parents, a child selector is
     * required.
     */
    public void populateChildren(Connection connection,
                                  List<Envelope<PARENTBUILDER>> parentBuilders,
                                  ChildrenSelector<CHILD, CHILDBUILDER> childrenSelector){

        logger.warning("populatechildren for " + parentBuilders.size());

        // This check is important, it avoids unnecessary SQL from being run.
        // Or worse, malformed SQL that performs a select in on an empty set
        if( parentBuilders.size() == 0 ){
            return;
        }


        // Run the SQL and get the children builder objects
        SqlRunner<CHILD,CHILDBUILDER> sqlRunner = new SqlRunner<>(connection, childDaoDescriptor);
        List<ChildrenDescriptor<CHILD,?,CHILDBUILDER, ?>> childrenDescriptorsList = childDaoDescriptor.childrenDescriptors();
        Supplier<CHILDBUILDER> supplier = childDaoDescriptor.supplier();
        List<Envelope<CHILDBUILDER>> childrenBuilders = childrenSelector.select(
                sqlBuilder,
                supplier,
                sqlRunner,
                parentChildColumnName(),
                childrenDescriptorsList);

        // partition the children by their parent IDs and populate them onto the
        // the parent builders
        Map<Long, List<CHILD>> childrenMapByParentId =
                buildChildrenMapByParentId(childrenBuilders, parentBuilders);
        for( Envelope<PARENTBUILDER> parentBuilderEnvelope : parentBuilders){
            long parentId = parentBuilderEnvelope.getId();
            List<CHILD> children = childrenMapByParentId.get(parentId);
            if( children == null ){
                children = new ArrayList<>();
            }

            logger.warning("FOUND CHILDREN about to set " + children.size());

            setter.accept(parentBuilderEnvelope.getItem(), children);
        }
    }

    private Map<Long, List<CHILD>> buildChildrenMapByParentId(List<Envelope<CHILDBUILDER>> childBuilders, List<Envelope<PARENTBUILDER>> parentBuilders){

        Map<Long, PARENT> parentsByIds = generateParentMap(parentBuilders);

        Map<Long, List<CHILD>> childrenMapByParentId = new HashMap<>();
        for( Envelope<CHILDBUILDER> childBuilderEnvelope : childBuilders ) {
            CHILDBUILDER childBuilder = childBuilderEnvelope.getItem();
            CHILD child = childBuilder().apply(childBuilder);
            Long parentId = childBuilderEnvelope.getParentId();
            PARENT parent = parentsByIds.get(parentId);
            if (!childrenMapByParentId.containsKey(parentId)) {
                childrenMapByParentId.put(parentId, new ArrayList<>());
            }
            List<CHILD> childList = childrenMapByParentId.get(parentId);
            parentSetter.accept(childBuilder, parent);
            childList.add(child);
        }
        return childrenMapByParentId;
    }

    private Map<Long, PARENT> generateParentMap(List<Envelope<PARENTBUILDER>> parentBuilders){
        Map<Long, PARENT> parentsByIds = new HashMap<>();
        for( Envelope<PARENTBUILDER> parentbuilderEnvelope : parentBuilders ) {
            if (parentbuilderEnvelope.getId() != null) {
                PARENT parent = parentBuildFunction.apply(parentbuilderEnvelope.getItem());
                parentsByIds.put(parentbuilderEnvelope.getId(), parent);
            }
        }
        return parentsByIds;
    }

    public void saveChildren(Connection connection, Envelope<PARENT> envelope) {

        PrimaryKey<CHILD, CHILDBUILDER> childPrimaryKey = childDaoDescriptor.primaryKey();

        PARENT item = envelope.getItem();

        SqlRunner<CHILD,CHILDBUILDER> sqlRunner = new SqlRunner<>(connection, childDaoDescriptor);

        List<CHILD> children = getter.apply(item);

        if( children == null ){
            children = Collections.emptyList();
        }
        Long parentId = envelope.getId();

        Set<Long> existingIds = findExistingChildrenIds(connection, parentId);

        for(CHILD child : children){
            Long childId = childPrimaryKey.getKey(child);
            if( childId == null ) {
                childId = sqlRunner.runSequenceNextValue(sqlBuilder.nextSequence());
                childPrimaryKey.optimisticSetKey(child, childId);
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

    private Set<Long> findExistingChildrenIds(Connection connection, Long parentId){
        String sql = sqlBuilder.selectChildIds();
        SqlRunner<PARENT,PARENTBUILDER> sqlRunner = new SqlRunner(connection);
        return sqlRunner.runSelectChildIds(sql, parentId);
    }

    private void deleteOrphans(Connection connection, Set<Long> badChildrenIds) {
        String preparedSql = sqlBuilder.delete();
        SqlRunner<CHILD, CHILDBUILDER> sqlRunner = new SqlRunner<>(connection);

        for(Long badId : badChildrenIds) {
            for( ChildrenDescriptor<CHILD,?,?,?> grandChildDescriptor : grandChildrenDescriptors()){
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

    private List<ChildrenDescriptor<CHILD,?,CHILDBUILDER,?>> grandChildrenDescriptors(){
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
