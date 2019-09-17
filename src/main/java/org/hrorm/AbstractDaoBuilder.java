package org.hrorm;

import java.sql.Connection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class AbstractDaoBuilder<ENTITY, ENTITYBUILDER, B extends AbstractDaoBuilder<?,?,?>>
        extends AbstractKeylessDaoBuilder<ENTITY, ENTITYBUILDER, B>
        implements DaoDescriptor<ENTITY,ENTITYBUILDER>, SchemaDescriptor<ENTITY, ENTITYBUILDER> {

    private ChildSelectStrategy childSelectStrategy = ChildSelectStrategy.Standard;

    public AbstractDaoBuilder(String tableName, Supplier<ENTITYBUILDER> supplier, Function<ENTITYBUILDER, ENTITY> buildFunction){
        super(tableName, supplier, buildFunction);
    }

    public Dao<ENTITY> buildDao(Connection connection){
        if( primaryKey() == null){
            throw new HrormException("Cannot create a Dao without a primary key.");
        }
        return new DaoImpl<>(connection, this);
    }

    /**
     * Describes a relationship between the object <code>ENTITY</code> and its several
     * child objects of type <code>U</code>.
     *
     * When hrorm inserts or updates objects with children it will attempt to
     * create, update, or delete child elements as necessary.
     *
     * The above should be emphasized. For the purposes of persistence, Hrorm
     * treats child objects (and grandchild and further generations of objects
     * transitively) as wholly owned by the parent object. On an update or
     * delete of the parent, the child objects will be updated or deleted as
     * necessary. Imagine a schema with a recipe entity and an ingredient
     * entity. The ingredient entities are children of various recipes. If
     * the recipe for bechamel is deleted, it makes no sense to have an
     * orphaned ingredient entry for one cup of butter. It will therefore be
     * deleted.
     *
     * @param getter The function on <code>ENTITY</code> that returns the children.
     * @param setter The function on <code>ENTITY</code> that consumes the children.
     * @param childDaoDescriptor The description of how the mapping for the subordinate elements
     *                      are persisted. Both <code>Dao</code> and <code>DaoBuilder</code>
     *                      objects implement the <code>DaoDescriptor</code> interface.
     * @param <CHILD> The type of the child data elements.
     * @param <CHILDBUILDER> The type of the builder of child data elements
     * @return This instance.
     */
    public <CHILD,CHILDBUILDER> B withChildren(Function<ENTITY, List<CHILD>> getter,
                                                                                 BiConsumer<ENTITYBUILDER, List<CHILD>> setter,
                                                                                 DaoDescriptor<CHILD,CHILDBUILDER> childDaoDescriptor){
        if( ! childDaoDescriptor.hasParent() ){
            throw new HrormException("Children must have a parent column");
        }

        ChildrenDescriptor<ENTITY, CHILD, ENTITYBUILDER, CHILDBUILDER> childrenDescriptor
                = new ChildrenDescriptor<>(getter, setter, childDaoDescriptor, primaryKey(), daoBuilderHelper.getBuildFunction());

        childrenDescriptors.add(childrenDescriptor);
        return (B) this;
    }

    /**
     * Indicator that the column is a reference to an owning parent object.
     *
     * @param columnName The name of the column that holds the foreign key reference.
     * @param getter The function to call for setting the parent onto the child.
     * @param setter The function to call for getting the parent from the child.
     * @param <P> The type of the parent object.
     * @return This instance.
     */
    public <P> B withParentColumn(String columnName, Function<ENTITY,P> getter, BiConsumer<ENTITYBUILDER,P> setter){
        ParentColumnImpl<ENTITY,P, ENTITYBUILDER,?> column = new ParentColumnImpl<>(columnName, daoBuilderHelper.getPrefix(), getter, setter);
        columnCollection.setParentColumn(column);
        return (B) this;
    }

    /**
     * Indicator that the column is a reference to an owning parent object.
     *
     * @param columnName The name of the column that holds the foreign key reference.
     * @return This instance.
     */
    public B withParentColumn(String columnName) {
        NoBackReferenceParentColumn<ENTITY, ?, ENTITYBUILDER, ?> column = new NoBackReferenceParentColumn<>(columnName, daoBuilderHelper.getPrefix());
        columnCollection.setParentColumn(column);
        return (B) this;
    }

    /**
     * Sets the most recent column added to this DaoBuilder to prevent it allowing
     * nulls on inserts or updates.
     *
     * @return This instance.
     */
    public B notNull(){
        columnCollection.setLastColumnAddedNotNull();
        return (B) this;
    }

    /**
     * Set the SQL type for the most recently added column. This is used
     * only when generating schema. If you wish to make sure that a column
     * has type "<code>VARCHAR2(100)</code>" instead of just "<code>TEXT</code>" you
     * can do so here. This make no difference to the queries that hrorm generates
     * or how results are parsed.
     *
     * @param sqlTypeName The name of the column in SQL.
     * @return This instance.
     */
    public B setSqlTypeName(String sqlTypeName){
        columnCollection.setLastColumnSqlTypeName(sqlTypeName);
        return (B) this;
    }

    /**
     * Describe a unique constraint on this entity. At run-time, hrorm makes
     * no use of this information, but it will be used during schema generation.
     * {@link Schema}.
     *
     * @param columnNames the names of the columns that are to be unique
     * @return This instance.
     */
    public B withUniqueConstraint(String ... columnNames){
        columnCollection.addUniquenConstraint(columnNames);
        return (B) this;
    }

    @Override
    public List<List<String>> uniquenessConstraints() {
        return columnCollection.getUniquenessConstraints();
    }

    @Override
    public ChildSelectStrategy childSelectStrategy() {
        return childSelectStrategy;
    }

    /**
     * Set the method used for selecting for child elements of the entity.
     *
     * @param childSelectStrategy The strategy to use during selects.
     * @return This instance.
     */
    public B withChildSelectStrategy(ChildSelectStrategy childSelectStrategy){
        this.childSelectStrategy = childSelectStrategy;
        return (B) this;
    }

    @Override
    public List<ChildrenDescriptor<ENTITY, ?, ENTITYBUILDER, ?>> childrenDescriptors() {
        return childrenDescriptors;
    }

    @Override
    public ParentColumn<ENTITY, ?, ENTITYBUILDER, ?> parentColumn() {
        return columnCollection.getParentColumn();
    }

    @Override
    public Queries buildQueries() {
        return new SqlBuilder<>(this);
    }
}
