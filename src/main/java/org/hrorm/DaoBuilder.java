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
 * @param <T> The class that the Dao will support.
 */
public class DaoBuilder<T> implements DaoDescriptor<T> {

    private final String tableName;
    private final List<TypedColumn<T>> columns = new ArrayList<>();
    private final List<JoinColumn<T,?>> joinColumns = new ArrayList<>();
    private final List<ChildrenDescriptor<T,?>> childrenDescriptors = new ArrayList<>();
    private PrimaryKey<T> primaryKey;
    private final Supplier<T> supplier;
    private final Prefixer prefixer;
    private final String myPrefix;

    /**
     * Create a new DaoBuilder instance.
     *
     * @param tableName The name of the table in the database.
     * @param supplier A mechanism (generally a constructor) for creating a new instance.
     */
    public DaoBuilder(String tableName, Supplier<T> supplier){
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
    public Supplier<T> supplier() {
        return supplier;
    }

    @Override
    public List<TypedColumn<T>> dataColumns() {
        return columns;
    }

    @Override
    public PrimaryKey<T> primaryKey() {
        return primaryKey;
    }

    @Override
    public List<ChildrenDescriptor<T, ?>> childrenDescriptors() {
        return childrenDescriptors;
    }

    public List<JoinColumn<T,?>> joinColumns() { return joinColumns; }

    /**
     * Creates a {@link Dao} for performing CRUD operations of type <code>T</code>.
     *
     * @param connection The SQL connection this <code>Dao</code> will use
     *                   for its operations.
     * @return The newly created <code>Dao</code>.
     */
    public Dao<T> buildDao(Connection connection){
        return new DaoImpl<>(connection, tableName, supplier, primaryKey, columns, joinColumns, childrenDescriptors);
    }

    /**
     * Describes a text or string data element.
     *
     * @param columnName The name of the column that holds the data element.
     * @param getter The function on <code>T</code> that returns the data element.
     * @param setter The function on <code>T</code> that consumes the data element.
     * @return This instance.
     */
    public DaoBuilder<T> withStringColumn(String columnName, Function<T, String> getter, BiConsumer<T, String> setter){
        TypedColumn<T> column = new StringColumn<>(columnName, myPrefix, getter, setter);
        columns.add(column);
        return this;
    }

    /**
     * Describes a numeric data element with no decimal or fractional part.
     *
     * @param columnName The name of the column that holds the data element.
     * @param getter The function on <code>T</code> that returns the data element.
     * @param setter The function on <code>T</code> that consumes the data element.
     * @return This instance.
     */
    public DaoBuilder<T> withIntegerColumn(String columnName, Function<T, Long> getter, BiConsumer<T, Long> setter){
        TypedColumn<T> column = new LongColumn<>(columnName, myPrefix, getter, setter);
        columns.add(column);
        return this;
    }

    /**
     * Describes a numeric data element with a decimal part.
     *
     * @param columnName The name of the column that holds the data element.
     * @param getter The function on <code>T</code> that returns the data element.
     * @param setter The function on <code>T</code> that consumes the data element.
     * @return This instance.
     */
    public DaoBuilder<T> withBigDecimalColumn(String columnName, Function<T, BigDecimal> getter, BiConsumer<T, BigDecimal> setter){
        TypedColumn<T> column = new BigDecimalColumn<>(columnName, myPrefix, getter, setter);
        columns.add(column);
        return this;
    }

    /**
     * Describes a data element with a particular type (like an enumeration) that
     * is persisted using a <code>String</code> representation.
     *
     * @param columnName The name of the column that holds the data element.
     * @param getter The function on <code>T</code> that returns the data element.
     * @param setter The function on <code>T</code> that consumes the data element.
     * @param converter A mechanism for converting between a <code>String</code> and
     *                  the type <code>E</code> that the object contains.
     * @param <E> The type being converted for persistence.
     * @return This instance.
     */
    public <E> DaoBuilder<T> withConvertingStringColumn(String columnName, Function<T, E> getter, BiConsumer<T, E> setter, Converter<E, String> converter){
        TypedColumn<T> column = new StringConverterColumn<>(columnName, myPrefix, getter, setter, converter);
        columns.add(column);
        return this;
    }

    /**
     * Describes a data element that represents a time stamp.
     *
     * @param columnName The name of the column that holds the data element.
     * @param getter The function on <code>T</code> that returns the data element.
     * @param setter The function on <code>T</code> that consumes the data element.
     * @return This instance.
     */
    public DaoBuilder<T> withLocalDateTimeColumn(String columnName, Function<T, LocalDateTime> getter, BiConsumer<T, LocalDateTime> setter){
        columns.add(new LocalDateTimeColumn<>(columnName, myPrefix, getter, setter));
        return this;
    }

    /**
     * Describes a data element that represents a true/false value. Boolean
     * elements are persisted to a text column with the single character
     * "T" or "F".
     *
     * @param columnName The name of the column that holds the data element.
     * @param getter The function on <code>T</code> that returns the data element.
     * @param setter The function on <code>T</code> that consumes the data element.
     * @return This instance.
     */
    public DaoBuilder<T> withBooleanColumn(String columnName, Function<T, Boolean> getter, BiConsumer<T, Boolean> setter){
        columns.add(new StringConverterColumn<>(columnName, myPrefix, getter, setter, BooleanConverter.INSTANCE));
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
     * @param getter The function on <code>T</code> that returns the data element.
     * @param setter The function on <code>T</code> that consumes the data element.
     * @param daoDescriptor The description of how the mapping for the subordinate element
     *                      is persisted. Both <code>Dao</code> and <code>DaoBuilder</code>
     *                      objects implement the <code>DaoDescriptor</code> interface.
     * @param <U> The type of the data element.
     * @return This instance.
     */
    public <U> DaoBuilder<T> withJoinColumn(String columnName, Function<T, U> getter, BiConsumer<T,U> setter, DaoDescriptor<U> daoDescriptor){
        JoinColumn<T,U> joinColumn = new JoinColumn<>(columnName, myPrefix, prefixer, getter, setter, daoDescriptor);
        joinColumns.add(joinColumn);
        return this;
    }

    /**
     * Describes a relationship between the object <code>T</code> and its several
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
     * @param parentChildColumnName The name on of the column <em>on the child table</em> that defines
     *                              the persisted link between the children objects of type <code>U</code>
     *                              and the parent object of type <code>T</code>.
     * @param parentSetter The function that allows the primary key of object <code>T</code> onto
     *                     the child object <code>U</code>.
     * @param getter The function on <code>T</code> that returns the children.
     * @param setter The function on <code>T</code> that consumes the children.
     * @param daoDescriptor The description of how the mapping for the subordinate elements
     *                      are persisted. Both <code>Dao</code> and <code>DaoBuilder</code>
     *                      objects implement the <code>DaoDescriptor</code> interface.
     * @param <U> The type of the child data elements.
     * @return This instance.
     */
    public <U> DaoBuilder<T> withChildren(String parentChildColumnName, BiConsumer<U,Long> parentSetter,
                                          Function<T, List<U>> getter, BiConsumer<T, List<U>> setter, DaoDescriptor<U> daoDescriptor){
        childrenDescriptors.add(
                new ChildrenDescriptor<>(parentChildColumnName, parentSetter, getter, setter, daoDescriptor, primaryKey)
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
    public DaoBuilder<T> withPrimaryKey(String columnName, String sequenceName, Function<T, Long> getter, BiConsumer<T, Long> setter){
        this.primaryKey = new PrimaryKeyImpl<>(columnName, myPrefix, getter, setter, sequenceName);
        columns.add(primaryKey);
        return this;
    }

}
