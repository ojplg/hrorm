package org.hrorm;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A DaoBuilder provides mechanisms for describing the relationship between
 * a Java type and the table(s) that will persist the data held in the class.
 *
 * @param <ENTITY> The class that the Dao will support.
 */
public class DaoBuilder<ENTITY> implements DaoDescriptor<ENTITY, ENTITY> {

    private final String tableName;
    private final List<Column<ENTITY, ENTITY>> columns = new ArrayList<>();
    private final List<JoinColumn<ENTITY,?, ENTITY,?>> joinColumns = new ArrayList<>();
    private final List<ChildrenDescriptor<ENTITY,?, ENTITY,?>> childrenDescriptors = new ArrayList<>();
    private PrimaryKey<ENTITY, ENTITY> primaryKey;
    private ParentColumn<ENTITY,?, ENTITY,?> parentColumn;
    private final Supplier<ENTITY> supplier;
    private final Prefixer prefixer;
    private final String myPrefix;

    private Column<ENTITY, ENTITY> lastColumnAdded;

    /**
     * Create a new DaoBuilder instance.
     *
     * @param tableName The name of the table in the database.
     * @param supplier A mechanism (generally a constructor) for creating a new instance.
     */
    public DaoBuilder(String tableName, Supplier<ENTITY> supplier){
        this.tableName = tableName;
        this.supplier = supplier;
        this.prefixer = new Prefixer();
        this.myPrefix = this.prefixer.nextPrefix();
    }

    @Override
    public String tableName() {
        return tableName;
    }

    @Override
    public Supplier<ENTITY> supplier() {
        return supplier;
    }

    @Override
    public List<Column<ENTITY, ENTITY>> dataColumns() {
        return columns;
    }

    @Override
    public PrimaryKey<ENTITY, ENTITY> primaryKey() {
        return primaryKey;
    }

    @Override
    public List<ChildrenDescriptor<ENTITY, ?, ENTITY, ?>> childrenDescriptors() {
        return childrenDescriptors;
    }

    @Override
    public ParentColumn<ENTITY, ?, ENTITY, ?> parentColumn() {
        return parentColumn;
    }

    public List<JoinColumn<ENTITY, ?, ENTITY, ?>> joinColumns() { return joinColumns; }

    @Override
    public Function<ENTITY, ENTITY> buildFunction() {
        return t -> t;
    }

    /**
     * Creates a {@link Dao} for performing CRUD operations of type <code>ENTITY</code>.
     *
     * @param connection The SQL connection this <code>Dao</code> will use
     *                   for its operations.
     * @return The newly created <code>Dao</code>.
     */
    public Dao<ENTITY> buildDao(Connection connection){
        if( primaryKey == null ){
            throw new HrormException("Cannot create a Dao without a primary key.");
        }
        return new DaoImpl<>(connection, tableName, supplier, primaryKey, columns, joinColumns, childrenDescriptors, parentColumn, t -> t);
    }

    /**
     * Describes a text or string data element.
     *
     * @param columnName The name of the column that holds the data element.
     * @param getter The function on <code>ENTITY</code> that returns the data element.
     * @param setter The function on <code>ENTITY</code> that consumes the data element.
     * @return This instance.
     */
    public DaoBuilder<ENTITY> withStringColumn(String columnName, Function<ENTITY, String> getter, BiConsumer<ENTITY, String> setter){
        Column<ENTITY, ENTITY> column = DataColumnFactory.stringColumn(columnName, myPrefix, getter, setter, true);
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
    public DaoBuilder<ENTITY> withIntegerColumn(String columnName, Function<ENTITY, Long> getter, BiConsumer<ENTITY, Long> setter){
        Column<ENTITY, ENTITY> column = DataColumnFactory.longColumn(columnName, myPrefix, getter, setter, true);
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
    public DaoBuilder<ENTITY> withBigDecimalColumn(String columnName, Function<ENTITY, BigDecimal> getter, BiConsumer<ENTITY, BigDecimal> setter){
        Column<ENTITY, ENTITY> column = DataColumnFactory.bigDecimalColumn(columnName, myPrefix, getter, setter, true);
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
    public <E> DaoBuilder<ENTITY> withConvertingStringColumn(String columnName, Function<ENTITY, E> getter, BiConsumer<ENTITY, E> setter, Converter<E, String> converter){
        Column<ENTITY, ENTITY> column = DataColumnFactory.stringConverterColumn(columnName, myPrefix, getter, setter, converter, true);
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
    public DaoBuilder<ENTITY> withLocalDateTimeColumn(String columnName, Function<ENTITY, LocalDateTime> getter, BiConsumer<ENTITY, LocalDateTime> setter){
        Column<ENTITY, ENTITY> column = DataColumnFactory.localDateTimeColumn(columnName, myPrefix, getter, setter, true);
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
    public DaoBuilder<ENTITY> withBooleanColumn(String columnName, Function<ENTITY, Boolean> getter, BiConsumer<ENTITY, Boolean> setter){
        Column<ENTITY, ENTITY> column = DataColumnFactory.stringConverterColumn(columnName, myPrefix, getter, setter, BooleanConverter.INSTANCE, true);
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
    public <U> DaoBuilder<ENTITY> withJoinColumn(String columnName, Function<ENTITY, U> getter, BiConsumer<ENTITY,U> setter, DaoDescriptor<U,?> daoDescriptor){
        JoinColumn<ENTITY,U, ENTITY,?> joinColumn = new JoinColumn<>(columnName, myPrefix, prefixer, getter, setter, daoDescriptor, true);
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
     * @param daoDescriptor The description of how the mapping for the subordinate elements
     *                      are persisted. Both <code>Dao</code> and <code>DaoBuilder</code>
     *                      objects implement the <code>DaoDescriptor</code> interface.
     * @param <U> The type of the child data elements.
     * @param <UB> The type of the builder of child data elements
     * @return This instance.
     */
    public <U,UB> DaoBuilder<ENTITY> withChildren(Function<ENTITY, List<U>> getter, BiConsumer<ENTITY, List<U>> setter, DaoDescriptor<U,UB> daoDescriptor){
        if( ! daoDescriptor.hasParent() ){
            throw new HrormException("Children must have a parent column");
        }
        childrenDescriptors.add(
                new ChildrenDescriptor<>(getter, setter, daoDescriptor, primaryKey, daoDescriptor.buildFunction(), buildFunction())
        );
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
        if ( this.primaryKey != null ){
            throw new HrormException("Attempt to set a second primary key");
        }
        this.primaryKey = new PrimaryKeyImpl<>(myPrefix, columnName, sequenceName, getter, setter);
        columns.add(primaryKey);
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
        if ( parentColumn != null ){
            throw new HrormException("Attempt to set a second parent");
        }
        ParentColumnImpl<ENTITY,P, ENTITY,?> column = new ParentColumnImpl<>(columnName, myPrefix, getter, setter);
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
    public <P> DaoBuilder<ENTITY> withParentColumn(String columnName){
        if ( parentColumn != null ){
            throw new HrormException("Attempt to set a second parent");
        }
        NoBackReferenceParentColumn<ENTITY,P, ENTITY,?> column = new NoBackReferenceParentColumn<>(columnName, myPrefix);
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
    public DaoBuilder<ENTITY> notNull(){
        if ( lastColumnAdded == null ){
            throw new HrormException("No column to set as not null has been added.");
        }
        lastColumnAdded.notNull();
        return this;
    }

}
