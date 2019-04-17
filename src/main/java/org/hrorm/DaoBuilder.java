package org.hrorm;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A DaoBuilder provides mechanisms for defining the relationship between
 * a Java type and the table(s) that will persist the data held in the class.
 *
 * <p>
 *     Also see {@link IndirectDaoBuilder} and {@link IndirectKeylessDaoBuilder}.
 * </p>
 *
 * @param <ENTITY> The class that the Dao will support.
 */
public class DaoBuilder<ENTITY> implements SchemaDescriptor<ENTITY, ENTITY> {

    private final ColumnCollection<ENTITY,ENTITY> columnCollection = new ColumnCollection<>();
    private final DaoBuilderHelper<ENTITY, ENTITY> daoBuilderHelper;
    private final List<ChildrenDescriptor<ENTITY,?, ENTITY,?>> childrenDescriptors = new ArrayList<>();

    /**
     * Create a new DaoBuilder instance.
     *
     * @param tableName The name of the table in the database.
     * @param supplier A mechanism (generally a constructor) for creating a new instance.
     */
    public DaoBuilder(String tableName, Supplier<ENTITY> supplier){
        this.daoBuilderHelper = new DaoBuilderHelper<>(tableName, supplier, t -> t);
    }

    @Override
    public String tableName() {
        return daoBuilderHelper.getTableName();
    }

    @Override
    public Supplier<ENTITY> supplier() {
        return daoBuilderHelper.getSupplier();
    }

    @Override
    public ColumnCollection<ENTITY, ENTITY> getColumnCollection(){
        return columnCollection;
    }

    @Override
    public List<ChildrenDescriptor<ENTITY, ?, ENTITY, ?>> childrenDescriptors() {
        return childrenDescriptors;
    }

    @Override
    public ParentColumn<ENTITY, ?, ENTITY, ?> parentColumn() {
        return columnCollection.getParentColumn();
    }

    @Override
    public Function<ENTITY, ENTITY> buildFunction() {
        return daoBuilderHelper.getBuildFunction();
    }

    /**
     * Creates a {@link Dao} for performing CRUD operations of type <code>ENTITY</code>.
     *
     * @param connection The SQL connection this <code>Dao</code> will use
     *                   for its operations.
     * @return The newly created <code>Dao</code>.
     */
    public Dao<ENTITY> buildDao(Connection connection){

        if( primaryKey() == null){
            throw new HrormException("Cannot create a Dao without a primary key.");
        }
        return new DaoImpl<>(connection, this);
    }

    /**
     * Build the SQL that will be used by <code>DAO</code> objects created by this builder.
     *
     * @return A container for the SQL
     */
    public Queries buildQueries() { return new SqlBuilder<>(this); }

    /**
     * Describes a text or string data element.
     *
     * @param columnName The name of the column that holds the data element.
     * @param getter The function on <code>ENTITY</code> that returns the data element.
     * @param setter The function on <code>ENTITY</code> that consumes the data element.
     * @return This instance.
     */
    public DaoBuilder<ENTITY> withStringColumn(String columnName, Function<ENTITY, String> getter, BiConsumer<ENTITY, String> setter){
        Column<ENTITY, ENTITY> column = DataColumnFactory.stringColumn(columnName, daoBuilderHelper.getPrefix(), getter, setter, true);
        columnCollection.addDataColumn(column);
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
    public DaoBuilder<ENTITY> withLongColumn(String columnName, Function<ENTITY, Long> getter, BiConsumer<ENTITY, Long> setter){
        Column<ENTITY, ENTITY> column = DataColumnFactory.longColumn(columnName, daoBuilderHelper.getPrefix(), getter, setter, true);
        columnCollection.addDataColumn(column);
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
    public DaoBuilder<ENTITY> withBigDecimalColumn(String columnName, Function<ENTITY, BigDecimal> getter, BiConsumer<ENTITY, BigDecimal> setter){
        Column<ENTITY, ENTITY> column = DataColumnFactory.bigDecimalColumn(columnName, daoBuilderHelper.getPrefix(), getter, setter, true);
        columnCollection.addDataColumn(column);
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
    public <E> DaoBuilder<ENTITY> withConvertingStringColumn(String columnName, Function<ENTITY, E> getter, BiConsumer<ENTITY, E> setter, Converter<E, String> converter){
        Column<ENTITY, ENTITY> column = DataColumnFactory.stringConverterColumn(columnName, daoBuilderHelper.getPrefix(), getter, setter, converter, true);
        columnCollection.addDataColumn(column);
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
    public DaoBuilder<ENTITY> withInstantColumn(String columnName, Function<ENTITY, Instant> getter, BiConsumer<ENTITY, Instant> setter){
        Column<ENTITY, ENTITY> column = DataColumnFactory.instantColumn(columnName, daoBuilderHelper.getPrefix(), getter, setter, true);
        columnCollection.addDataColumn(column);
        return this;
    }

    /**
     * Describes a data element that represents a true/false value that is
     * backed by a SQL boolean column.
     *
     * @param columnName The name of the column that holds the data element.
     * @param getter The function on <code>ENTITY</code> that returns the data element.
     * @param setter The function on <code>ENTITY</code> that consumes the data element.
     * @return This instance.
     */
    public DaoBuilder<ENTITY> withBooleanColumn(String columnName, Function<ENTITY, Boolean> getter, BiConsumer<ENTITY, Boolean> setter){
        Column<ENTITY, ENTITY> column = DataColumnFactory.booleanColumn(columnName, daoBuilderHelper.getPrefix(), getter, setter, true);
        columnCollection.addDataColumn(column);
        return this;
    }

    /**
     * Describes a data element that represents a true/false value
     * and is backed by a column holding a String value. Boolean
     * elements are persisted with the single character
     * "T" or "F".
     *
     * @param columnName The name of the column that holds the data element.
     * @param getter The function on <code>ENTITY</code> that returns the data element.
     * @param setter The function on <code>ENTITY</code> that consumes the data element.
     * @return This instance.
     */
    public DaoBuilder<ENTITY> withStringBooleanColumn(String columnName, Function<ENTITY, Boolean> getter, BiConsumer<ENTITY, Boolean> setter){
        Column<ENTITY, ENTITY> column = DataColumnFactory.textBackedBooleanColumn(columnName, daoBuilderHelper.getPrefix(), getter, setter, true);
        columnCollection.addDataColumn(column);
        return this;
    }

    /**
     * Describes a data element that represents a true/false value
     * and is backed by a column holding an integer value. Boolean
     * elements are persisted as 0 (false) or 1 (true).
     *
     * @param columnName The name of the column that holds the data element.
     * @param getter The function on <code>ENTITY</code> that returns the data element.
     * @param setter The function on <code>ENTITY</code> that consumes the data element.
     * @return This instance.
     */
    public DaoBuilder<ENTITY> withIntegerBooleanColumn(String columnName, Function<ENTITY, Boolean> getter, BiConsumer<ENTITY, Boolean> setter){
        Column<ENTITY, ENTITY> column = DataColumnFactory.longBackedBooleanColumn(columnName, daoBuilderHelper.getPrefix(), getter, setter, true);
        columnCollection.addDataColumn(column);
        return this;
    }


    /**
     * <p>Describes a data element that is represented by an <code>Object</code> of some
     * other type <code>U</code> with its own table for persistence.</p>
     *
     * <p>Join columns describe entities that have their own independent existence and
     * their persistence is a pre-requisite for the persistence of dependent objects.</p>
     *
     * <p>Imagine a schema that describes cities and states. Every city entity should
     * be assigned to exactly one state. If the city is modified or deleted, it
     * has no repercusions to the state entity. The only thing that can happen is
     * that the city is assigned to a new state.</p>
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
    public <U> DaoBuilder<ENTITY> withJoinColumn(String columnName, Function<ENTITY, U> getter, BiConsumer<ENTITY,U> setter, DaoDescriptor<U,?> daoDescriptor){
        JoinColumn<ENTITY,U, ENTITY,?> joinColumn = new JoinColumn<>(columnName, daoBuilderHelper.getPrefix(), daoBuilderHelper.getPrefixer(), getter, setter, daoDescriptor, true);
        columnCollection.addJoinColumn(joinColumn);
        return this;
    }

    /**
     * Describes a relationship between the object <code>ENTITY</code> and its several
     * child objects of type <code>U</code>.
     *
     * <p>
     * When hrorm inserts or updates objects with children it will attempt to
     * create, update, or delete child elements as necessary.</p>
     *
     * <p>The above should be emphasized. For the purposes of persistence, Hrorm
     * treats child objects (and grandchild and further generations of objects
     * transitively) as wholly owned by the parent object. On an update or
     * delete of the parent, the child objects will be updated or deleted as
     * necessary. Imagine a schema with a recipe entity and an ingredient
     * entity. The ingredient entities are children of various recipes. If
     * the recipe for bechamel is deleted, it makes no sense to have an
     * orphaned ingredient entry for one cup of butter. It will therefore be
     * deleted.</p>
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
    public <CHILD,CHILDBUILDER> DaoBuilder<ENTITY> withChildren(Function<ENTITY, List<CHILD>> getter, BiConsumer<ENTITY, List<CHILD>> setter, DaoDescriptor<CHILD,CHILDBUILDER> childDaoDescriptor){
        if( ! childDaoDescriptor.hasParent() ){
            throw new HrormException("Children must have a parent column");
        }

        ChildrenDescriptor<ENTITY, CHILD, ENTITY, CHILDBUILDER> childrenDescriptor
                = new ChildrenDescriptor<>(getter, setter, childDaoDescriptor, primaryKey(), daoBuilderHelper.getBuildFunction());

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
    public DaoBuilder<ENTITY> withPrimaryKey(String columnName, String sequenceName, Function<ENTITY, Long> getter, BiConsumer<ENTITY, Long> setter){
        PrimaryKey<ENTITY,ENTITY> primaryKey = new DirectPrimaryKey<>(daoBuilderHelper.getPrefix(), columnName, sequenceName, getter, setter);
        columnCollection.setPrimaryKey(primaryKey);
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
    public <P> DaoBuilder<ENTITY> withParentColumn(String columnName, Function<ENTITY,P> getter, BiConsumer<ENTITY,P> setter){
        ParentColumnImpl<ENTITY,P, ENTITY,?> column = new ParentColumnImpl<>(columnName, daoBuilderHelper.getPrefix(), getter, setter);
        columnCollection.setParentColumn(column);
        return this;
    }

    /**
     * Indicator that the column is a reference to an owning parent object.
     *
     * @param columnName The name of the column that holds the foreign key reference.
     * @return This instance.
     */
    public DaoBuilder<ENTITY> withParentColumn(String columnName){
        NoBackReferenceParentColumn<ENTITY, ?, ENTITY, ?> column = new NoBackReferenceParentColumn<>(columnName, daoBuilderHelper.getPrefix());
        columnCollection.setParentColumn(column);
        return this;
    }


    /**
     * Sets the most recent column added to this DaoBuilder to prevent it allowing
     * nulls on inserts or updates.
     *
     * @return This instance.
     */
    public DaoBuilder<ENTITY> notNull(){
        columnCollection.setLastColumnAddedNotNull();
        return this;
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
    public DaoBuilder<ENTITY> setSqlTypeName(String sqlTypeName){
        columnCollection.setLastColumnSqlTypeName(sqlTypeName);
        return this;
    }

    /**
     * Describes a data element of type <code>T</code> that can be stored in
     * a <code>GenericColumn</code>.
     *
     * <p>
     * This interface exists to allow clients to inject whatever column types
     * they need into Hrorm.
     * </p>
     *
     * @param columnName The name of the column in the table that holds the data element.
     * @param getter The function to call to get the value from an object instance.
     * @param setter The function to call to set the value onto an object instance.
     * @param genericColumn The column that supports type <code>T</code>.
     * @param <T> The type of the data element on the entity.
     * @return This instance.
     */
    public <T> DaoBuilder<ENTITY> withGenericColumn(String columnName,
                                                    Function<ENTITY, T> getter,
                                                    BiConsumer<ENTITY, T> setter,
                                                    GenericColumn<T> genericColumn){
        Column<ENTITY, ENTITY> column = DataColumnFactory.genericColumn(columnName, daoBuilderHelper.getPrefix(), getter, setter, genericColumn, true);
        columnCollection.addDataColumn(column);
        return this;
    }

    /**
     * Describes a data element of type <code>U</code> that can be stored in
     * a <code>GenericColumn</code> that stores objects of type <code>T</code>.
     *
     * <p>
     * This interface exists to allow clients to inject whatever column types
     * they need into Hrorm.
     * </p>
     *
     * @param columnName The name of the column in the table that holds the data element.
     * @param getter The function to call to get the value from an object instance.
     * @param setter The function to call to set the value onto an object instance.
     * @param genericColumn The column that supports type <code>T</code>.
     * @param converter A converter that can translate between types <code>T</code> and <code>U</code>.
     * @param <T> The type of the data element that can be persisted.
     * @param <U> The type of the data element as it exists on the entity object.
     * @return This instance.
     */
    public <T,U> DaoBuilder<ENTITY> withConvertedGenericColumn(String columnName,
                                                               Function<ENTITY, U> getter,
                                                               BiConsumer<ENTITY, U> setter,
                                                               GenericColumn<T> genericColumn,
                                                               Converter<U,T> converter){
        Column<ENTITY, ENTITY> column = DataColumnFactory.convertedGenericColumn(columnName, daoBuilderHelper.getPrefix(), getter, setter, genericColumn, converter,true);
        columnCollection.addDataColumn(column);
        return this;
    }

    /**
     * Describe a unique constraint on this entity. At run-time, hrorm makes
     * no use of this information, but it will be used during schema generation.
     * {@link Schema}.
     *
     * @param columnNames the names of the columns that are to be unique
     * @return This instance.
     */
    public DaoBuilder<ENTITY> withUniqueConstraint(String ... columnNames){
        columnCollection.addUniquenConstraint(columnNames);
        return this;
    }

    @Override
    public List<List<String>> uniquenessConstraints() {
        return columnCollection.getUniquenessConstraints();
    }
}
