package org.hrorm;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;


/**
 * An <code>IndirectDaoBuilder</code> is used for times when the class representing
 * the persisted entity is immutable. It allows the relationships between the database
 * table, the entity class, and the entity's builder class to be defined.
 *
 * <p>
 *     Also see {@link DaoBuilder}.
 * </p>
 *
 * @param <ENTITY> The type of the class that the <code>Dao</code> will support.
 * @param <BUILDER> The type of the class that can be used to construct new <code>ENTITY</code>
 *                 instances and accept individual data elements.
 */
public class IndirectDaoBuilder<ENTITY, BUILDER>  implements DaoDescriptor<ENTITY, BUILDER> {

    private final String tableName;
    private final String myPrefix;
    private final Function<BUILDER, ENTITY> buildFunction;
    private final Supplier<BUILDER> supplier;

    private final Prefixer prefixer;

    private final List<Column<ENTITY, BUILDER>> columns = new ArrayList<>();
    private final List<JoinColumn<ENTITY,?, BUILDER,?>> joinColumns = new ArrayList<>();
    private final List<ChildrenDescriptor<ENTITY,?, BUILDER,?>> childrenDescriptors = new ArrayList<>();

    private PrimaryKey<ENTITY, BUILDER> primaryKey;
    private ParentColumn<ENTITY,?, BUILDER,?> parentColumn;

    private Column<ENTITY, BUILDER> lastColumnAdded;

    static class BuilderHolder<T,TB> {
        IndirectDaoBuilder<T,TB> daoBuilder;
        Consumer<PrimaryKey<T,TB>> primaryKeyConsumer;
        String myPrefix;
    }

    static <T> BuilderHolder<T,T> forDirectDaoBuilder(String tableName, Supplier<T> supplier){
        IndirectDaoBuilder<T,T> daoBuilder = new IndirectDaoBuilder<>(tableName, supplier, t-> t);
        BuilderHolder<T,T> holder = new BuilderHolder<>();
        holder.daoBuilder = daoBuilder;
        holder.primaryKeyConsumer = daoBuilder::acceptPrimaryKey;
        holder.myPrefix = daoBuilder.myPrefix;
        return holder;
    }

    private void acceptPrimaryKey(PrimaryKey<ENTITY,BUILDER> primaryKey){
        if ( this.primaryKey != null ){
            throw new HrormException("Attempt to set a second primary key");
        }
        this.primaryKey = primaryKey;
        columns.add(primaryKey);
    }

    /**
     * Create a new DaoBuilder instance.
     *
     * @param tableName The name of the table in the database.
     * @param supplier A mechanism (generally a constructor) for creating a new instance.
     * @param buildFunction How to create an instance of the entity class from its builder.
     */
    public IndirectDaoBuilder(String tableName, Supplier<BUILDER> supplier, Function<BUILDER, ENTITY> buildFunction){
        this.tableName = tableName;
        this.supplier = supplier;
        this.prefixer = new Prefixer();
        this.myPrefix = this.prefixer.nextPrefix();
        this.buildFunction = buildFunction;
    }

    @Override
    public String tableName() {
        return tableName;
    }

    @Override
    public Supplier<BUILDER> supplier() {
        return supplier;
    }

    @Override
    public List<Column<ENTITY, BUILDER>> dataColumns() {
        return columns;
    }

    @Override
    public PrimaryKey<ENTITY, BUILDER> primaryKey() {
        return primaryKey;
    }

    @Override
    public List<ChildrenDescriptor<ENTITY, ?, BUILDER, ?>> childrenDescriptors() {
        return childrenDescriptors;
    }

    @Override
    public ParentColumn<ENTITY, ?, BUILDER, ?> parentColumn() {
        return parentColumn;
    }

    public List<JoinColumn<ENTITY, ?, BUILDER, ?>> joinColumns() { return joinColumns; }

    @Override
    public Function<BUILDER, ENTITY> buildFunction() {
        return buildFunction;
    }

    /**
     * Creates a {@link Dao} for performing CRUD operations of type <code>ENTITY</code>.
     *
     * @param connection The SQL connection this <code>Dao</code> will use
     *                   for its operations.
     * @return The newly created <code>Dao</code>.
     */
    public Dao<ENTITY> buildDao(Connection connection){
        if( primaryKey == null){
            throw new HrormException("Cannot create a Dao without a primary key.");
        }
        return new DaoImpl<>(connection, this);
    }

    /**
     * Creates a {@link Dao} for performing CRUD operations of type <code>ENTITY</code>.
     *
     * @param connection The SQL connection this <code>Dao</code> will use
     *                   for its operations.
     * @return The newly created <code>Dao</code>.
     */
    public KeylessDao<ENTITY> buildKeylessDao(Connection connection){
        if( primaryKey == null){
            return new KeylessDaoImpl<>(connection, this);
        }
        return buildDao(connection);
    }

    /**
     * Build the SQL that will be used by <code>DAO</code> objects created by this builder.
     *
     * @return A container for the SQL
     */
    public Queries buildQueries(){
        return new SqlBuilder<>(this);
    }

    /**
     * Describes a text or string data element.
     *
     * @param columnName The name of the column that holds the data element.
     * @param getter The function on <code>ENTITY</code> that returns the data element.
     * @param setter The function on <code>ENTITY</code> that consumes the data element.
     * @return This instance.
     */
    public IndirectDaoBuilder<ENTITY, BUILDER> withStringColumn(String columnName, Function<ENTITY, String> getter, BiConsumer<BUILDER, String> setter){
        Column<ENTITY, BUILDER> column = DataColumnFactory.stringColumn(columnName, myPrefix, getter, setter, true);
        columns.add(column);
        lastColumnAdded = column;
        return this;
    }

    /**
     * Describes a numeric data element with no decimal or fractional part.
     *
     * @param columnName The name of the column that holds the data element.
     * @param getter The function on <code>ENTITY</code> that returns the data element.
     * @param setter The function on <code>ENTITY</code> that consumes the data element.
     * @return This instance.
     */
    public IndirectDaoBuilder<ENTITY, BUILDER> withIntegerColumn(String columnName, Function<ENTITY, Long> getter, BiConsumer<BUILDER, Long> setter){
        Column<ENTITY, BUILDER> column = DataColumnFactory.longColumn(columnName, myPrefix, getter, setter, true);
        columns.add(column);
        lastColumnAdded = column;
        return this;
    }

    /**
     * Describes a numeric data element with a decimal part.
     *
     * @param columnName The name of the column that holds the data element.
     * @param getter The function on <code>ENTITY</code> that returns the data element.
     * @param setter The function on <code>ENTITY</code> that consumes the data element.
     * @return This instance.
     */
    public IndirectDaoBuilder<ENTITY, BUILDER> withBigDecimalColumn(String columnName, Function<ENTITY, BigDecimal> getter, BiConsumer<BUILDER, BigDecimal> setter){
        Column<ENTITY, BUILDER> column = DataColumnFactory.bigDecimalColumn(columnName, myPrefix, getter, setter, true);
        columns.add(column);
        lastColumnAdded = column;
        return this;
    }

    /**
     * Describes a data element with a particular type (like an enumeration) that
     * is persisted using a <code>String</code> representation.
     *
     * @param columnName The name of the column that holds the data element.
     * @param getter The function on <code>ENTITY</code> that returns the data element.
     * @param setter The function on <code>ENTITY</code> that consumes the data element.
     * @param converter A mechanism for converting between a <code>String</code> and
     *                  the type <code>E</code> that the object contains.
     * @param <E> The type being converted for persistence.
     * @return This instance.
     */
    public <E> IndirectDaoBuilder<ENTITY, BUILDER> withConvertingStringColumn(String columnName, Function<ENTITY, E> getter, BiConsumer<BUILDER, E> setter, Converter<E, String> converter){
        Column<ENTITY, BUILDER> column = DataColumnFactory.stringConverterColumn(columnName, myPrefix, getter, setter, converter, true);
        columns.add(column);
        lastColumnAdded = column;
        return this;
    }

    /**
     * Describes a data element that represents a time stamp.
     *
     * @param columnName The name of the column that holds the data element.
     * @param getter The function on <code>ENTITY</code> that returns the data element.
     * @param setter The function on <code>ENTITY</code> that consumes the data element.
     * @return This instance.
     */
    public IndirectDaoBuilder<ENTITY, BUILDER> withLocalDateTimeColumn(String columnName, Function<ENTITY, LocalDateTime> getter, BiConsumer<BUILDER, LocalDateTime> setter){
        Column<ENTITY, BUILDER> column = DataColumnFactory.localDateTimeColumn(columnName, myPrefix, getter, setter, true);
        columns.add(column);
        lastColumnAdded = column;
        return this;
    }

    /**
     * Describes a data element that represents a true/false value. Boolean
     * elements are persisted to a text column with the single character
     * "ENTITY" or "F".
     *
     * @param columnName The name of the column that holds the data element.
     * @param getter The function on <code>ENTITY</code> that returns the data element.
     * @param setter The function on <code>ENTITY</code> that consumes the data element.
     * @return This instance.
     */
    public IndirectDaoBuilder<ENTITY, BUILDER> withBooleanColumn(String columnName, Function<ENTITY, Boolean> getter, BiConsumer<BUILDER, Boolean> setter){
        Column<ENTITY, BUILDER> column = DataColumnFactory.stringConverterColumn(columnName, myPrefix, getter, setter, BooleanConverter.INSTANCE, true);
        columns.add(column);
        lastColumnAdded = column;
        return this;
    }

    /**
     * Describes a data element that is represented by an <code>Object</code> of some
     * other type <code>U</code> with its own table for persistence.
     *
     * Join columns describe entities that have their own independent existence and
     * their persistence is a pre-requisite for the persistence of dependent objects.
     *
     * Imagine a schema that describes cities and states. Every city entity should
     * be assigned to exactly one state. If the city is modified or deleted, it
     * has no repercusions to the state entity. The only thing that can happen is
     * that the city is assigned to a new state.
     *
     * @param columnName The name of the column with the foreign key to the other table.
     *                   This column must be an integer type and must reference the primary
     *                   key of the other table.
     * @param getter The function on <code>ENTITY</code> that returns the data element.
     * @param setter The function on <code>ENTITY</code> that consumes the data element.
     * @param daoDescriptor The description of how the mapping for the subordinate element
     *                      is persisted. Both <code>Dao</code> and <code>DaoBuilder</code>
     *                      objects implement the <code>DaoDescriptor</code> interface.
     * @param <U> The type of the data element.
     * @return This instance.
     */
    public <U> IndirectDaoBuilder<ENTITY, BUILDER> withJoinColumn(String columnName, Function<ENTITY, U> getter, BiConsumer<BUILDER,U> setter, DaoDescriptor<U,?> daoDescriptor){
        JoinColumn<ENTITY,U, BUILDER,?> joinColumn = new JoinColumn<>(columnName, myPrefix, prefixer, getter, setter, daoDescriptor, true);
        joinColumns.add(joinColumn);
        lastColumnAdded = joinColumn;
        return this;
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
     * Contrast this behavior with the join column functionality, which describes
     * the situation wherein the object makes no sense without the joined relation.
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
    public <CHILD,CHILDBUILDER> IndirectDaoBuilder<ENTITY, BUILDER> withChildren(Function<ENTITY, List<CHILD>> getter,
                                                                   BiConsumer<BUILDER, List<CHILD>> setter,
                                                                   DaoDescriptor<CHILD,CHILDBUILDER> childDaoDescriptor){
        if( ! childDaoDescriptor.hasParent() ){
            throw new HrormException("Children must have a parent column");
        }

        ChildrenDescriptor<ENTITY, CHILD, BUILDER, CHILDBUILDER> childrenDescriptor
                = new ChildrenDescriptor<>(getter, setter, childDaoDescriptor, primaryKey, buildFunction);

        childrenDescriptors.add(childrenDescriptor);
        return this;
    }

    /**
     * Set data about the primary key of the table for this type. Hrorm demands that primary keys be
     * sequence numbers from the database. GUIDs and other constructions are not allowed. All
     * Daos must have a primary key.
     *
     * @param columnName The name of the column in the table that holds the primary key.
     * @param sequenceName The name of the sequence that will provide new keys.
     * @param getter The function to call to get the primary key value from an object instance.
     * @param setter The function to call to set the primary key value to an object instance.
     * @return This instance.
     */
    public IndirectDaoBuilder<ENTITY, BUILDER> withPrimaryKey(String columnName, String sequenceName, Function<ENTITY, Long> getter, BiConsumer<BUILDER, Long> setter){
        PrimaryKey<ENTITY, BUILDER> key = new IndirectPrimaryKey<>(myPrefix, columnName, sequenceName, getter, setter);
        acceptPrimaryKey(key);
        return this;
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
    public <P> IndirectDaoBuilder<ENTITY, BUILDER> withParentColumn(String columnName, Function<ENTITY,P> getter, BiConsumer<BUILDER,P> setter){
        if ( parentColumn != null ){
            throw new HrormException("Attempt to set a second parent");
        }
        ParentColumnImpl<ENTITY,P, BUILDER,?> column = new ParentColumnImpl<>(columnName, myPrefix, getter, setter);
        lastColumnAdded = column;
        parentColumn = column;
        return this;
    }

    /**
     * Indicator that the column is a reference to an owning parent object.
     *
     * @param columnName The name of the column that holds the foreign key reference.
     * @param <P> The type of the parent object.
     * @return This instance.
     */
    public <P> IndirectDaoBuilder<ENTITY, BUILDER> withParentColumn(String columnName){
        if ( parentColumn != null ){
            throw new HrormException("Attempt to set a second parent");
        }
        NoBackReferenceParentColumn<ENTITY,P, BUILDER,?> column = new NoBackReferenceParentColumn<>(columnName, myPrefix);
        lastColumnAdded = column;
        parentColumn = column;
        return this;
    }


    /**
     * Sets the most recent column added to this DaoBuilder to prevent it allowing
     * nulls on inserts or updates.
     *
     * @return This instance.
     */
    public IndirectDaoBuilder<ENTITY, BUILDER> notNull(){
        if ( lastColumnAdded == null ){
            throw new HrormException("No column to set as not null has been added.");
        }
        lastColumnAdded.notNull();
        return this;
    }

    public String getMyPrefix(){
        return myPrefix;
    }

}
