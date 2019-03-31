package org.hrorm;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/***
 * An <code>IndirectKeylessDaoBuilder</code> provides mechanisms for defining the relationship between
 * a Java type and the table that backs it.
 *
 * <p>
 *    Using this builder (as opposed to the {@link DaoBuilder} or {@link IndirectDaoBuilder})
 *    will allow support for entities that do not have primary keys, but that support comes at
 *    a price. A <code>KeylessDao</code> supports fewer methods than a standard <code>Dao</code>
 *    and these entities cannot be joined to other objects or be children to other objects.
 *    In general, the regular variants should be preferred
 * </p>
 *
 * @param <ENTITY> The class that the <code>KeylessDao</code> will support.
 * @param <BUILDER> The class that builds the <code>ENTITY</code> type.
 */
public class IndirectKeylessDaoBuilder<ENTITY, BUILDER> implements KeylessDaoDescriptor<ENTITY, BUILDER> {

    private final String tableName;
    private final String myPrefix;
    private final Function<BUILDER, ENTITY> buildFunction;
    private final Supplier<BUILDER> supplier;

    private final Prefixer prefixer;

    private final List<Column<ENTITY, BUILDER>> columns = new ArrayList<>();
    private final List<JoinColumn<ENTITY,?, BUILDER,?>> joinColumns = new ArrayList<>();

    private ParentColumn<ENTITY,?, BUILDER,?> parentColumn;

    private Column<ENTITY, BUILDER> lastColumnAdded;

    public IndirectKeylessDaoBuilder(String table, Supplier<BUILDER> supplier, Function<BUILDER, ENTITY> buildFunction) {
        this.tableName = table;
        this.supplier = supplier;
        this.buildFunction = buildFunction;
        this.prefixer = new Prefixer();
        this.myPrefix = prefixer.nextPrefix();
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
    public List<ChildrenDescriptor<ENTITY, ?, BUILDER, ?>> childrenDescriptors() {
        return Collections.emptyList();
    }

    @Override
    public ParentColumn<ENTITY, ?, BUILDER, ?> parentColumn() {
        return parentColumn;
    }

    public String getPrefix(){
        return myPrefix;
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
    public KeylessDao<ENTITY> buildDao(Connection connection){
        return new KeylessDaoImpl(connection, this);
    }

    /**
     * Describes a text or string data element.
     *
     * @param columnName The name of the column that holds the data element.
     * @param getter The function on <code>ENTITY</code> that returns the data element.
     * @param setter The function on <code>ENTITY</code> that consumes the data element.
     * @return This instance.
     */
    public IndirectKeylessDaoBuilder<ENTITY, BUILDER> withStringColumn(String columnName, Function<ENTITY, String> getter, BiConsumer<BUILDER, String> setter){
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
    public IndirectKeylessDaoBuilder<ENTITY, BUILDER> withIntegerColumn(String columnName, Function<ENTITY, Long> getter, BiConsumer<BUILDER, Long> setter){
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
    public IndirectKeylessDaoBuilder<ENTITY, BUILDER> withBigDecimalColumn(String columnName, Function<ENTITY, BigDecimal> getter, BiConsumer<BUILDER, BigDecimal> setter){
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
    public <E> IndirectKeylessDaoBuilder<ENTITY, BUILDER> withConvertingStringColumn(String columnName, Function<ENTITY, E> getter, BiConsumer<BUILDER, E> setter, Converter<E, String> converter){
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
    public IndirectKeylessDaoBuilder<ENTITY, BUILDER> withInstantColumn(String columnName, Function<ENTITY, Instant> getter, BiConsumer<BUILDER, Instant> setter){
        Column<ENTITY, BUILDER> column = DataColumnFactory.instantColumn(columnName, myPrefix, getter, setter, true);
        columns.add(column);
        lastColumnAdded = column;
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
    public IndirectKeylessDaoBuilder<ENTITY, BUILDER> withBooleanColumn(String columnName, Function<ENTITY, Boolean> getter, BiConsumer<BUILDER, Boolean> setter){
        Column<ENTITY, BUILDER> column = DataColumnFactory.booleanColumn(columnName, myPrefix, getter, setter, true);
        columns.add(column);
        lastColumnAdded = column;
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
    public IndirectKeylessDaoBuilder<ENTITY, BUILDER> withStringBooleanColumn(String columnName, Function<ENTITY, Boolean> getter, BiConsumer<BUILDER, Boolean> setter){
        Column<ENTITY, BUILDER> column = DataColumnFactory.textBackedBooleanColumn(columnName, myPrefix, getter, setter, true);
        columns.add(column);
        lastColumnAdded = column;
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
    public IndirectKeylessDaoBuilder<ENTITY, BUILDER> withIntegerBooleanColumn(String columnName, Function<ENTITY, Boolean> getter, BiConsumer<BUILDER, Boolean> setter){
        Column<ENTITY, BUILDER> column = DataColumnFactory.integerConverterColumn(columnName, myPrefix, getter, setter, BooleanLongConverter.INSTANCE, true);
        columns.add(column);
        lastColumnAdded = column;
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
    public <U> IndirectKeylessDaoBuilder<ENTITY, BUILDER> withJoinColumn(String columnName, Function<ENTITY, U> getter, BiConsumer<BUILDER,U> setter, DaoDescriptor<U,?> daoDescriptor){
        JoinColumn<ENTITY,U, BUILDER,?> joinColumn = new JoinColumn<>(columnName, myPrefix, prefixer, getter, setter, daoDescriptor, true);
        joinColumns.add(joinColumn);
        lastColumnAdded = joinColumn;
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
    public <P> IndirectKeylessDaoBuilder<ENTITY, BUILDER> withParentColumn(String columnName, Function<ENTITY,P> getter, BiConsumer<BUILDER,P> setter){
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
     * @return This instance.
     */
    public <P> IndirectKeylessDaoBuilder<ENTITY, BUILDER> withParentColumn(String columnName) {
        if (parentColumn != null) {
            throw new HrormException("Attempt to set a second parent");
        }
        NoBackReferenceParentColumn<ENTITY, P, BUILDER, ?> column = new NoBackReferenceParentColumn<>(columnName, myPrefix);
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
    public IndirectKeylessDaoBuilder<ENTITY, BUILDER> notNull(){
        if ( lastColumnAdded == null ){
            throw new HrormException("No column to set as not null has been added.");
        }
        lastColumnAdded.notNull();
        return this;
    }


    public <T> IndirectKeylessDaoBuilder<ENTITY, BUILDER> withGenericColumn(String columnName,
                                                                     Function<ENTITY, T> getter,
                                                                     BiConsumer<BUILDER, T> setter,
                                                                     GenericColumn<T> genericColumn){
        Column<ENTITY, BUILDER> column = DataColumnFactory.genericColumn(columnName, myPrefix, getter, setter, genericColumn, true);
        columns.add(column);
        lastColumnAdded = column;
        return this;
    }


    public <T,U> IndirectKeylessDaoBuilder<ENTITY, BUILDER> withConvertedGenericColumn(String columnName,
                                                                                Function<ENTITY, U> getter,
                                                                                BiConsumer<BUILDER, U> setter,
                                                                                GenericColumn<T> genericColumn,
                                                                                Converter<U,T> converter){
        Column<ENTITY, BUILDER> column = DataColumnFactory.convertedGenericColumn(columnName, myPrefix, getter, setter, genericColumn, converter, true);
        columns.add(column);
        lastColumnAdded = column;
        return this;
    }

}
