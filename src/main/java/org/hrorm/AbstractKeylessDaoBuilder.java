package org.hrorm;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A base class for the various Dao building classes.
 *
 * @param <ENTITY> The entity being persisted.
 * @param <ENTITYBUILDER> The builder class of the entity.
 * @param <BUILDER> The type of the concrete class extending this.
 */
public abstract class AbstractKeylessDaoBuilder<ENTITY, ENTITYBUILDER, BUILDER extends AbstractKeylessDaoBuilder<ENTITY, ENTITYBUILDER, BUILDER>>
    implements KeylessDaoDescriptor<ENTITY, ENTITYBUILDER> {

    protected final ColumnCollection<ENTITY,ENTITYBUILDER> columnCollection = new ColumnCollection<>();
    protected final DaoBuilderHelper<ENTITY, ENTITYBUILDER> daoBuilderHelper;
    protected final List<ChildrenDescriptor<ENTITY,?, ENTITYBUILDER,?>> childrenDescriptors = new ArrayList<>();

    public AbstractKeylessDaoBuilder(String tableName, Supplier<ENTITYBUILDER> supplier, Function<ENTITYBUILDER, ENTITY> buildFunction){
        this.daoBuilderHelper = new DaoBuilderHelper<>(tableName, supplier, buildFunction);
    }

    @Override
    public String tableName() {
        return daoBuilderHelper.getTableName();
    }

    @Override
    public Supplier<ENTITYBUILDER> supplier() {
        return daoBuilderHelper.getSupplier();
    }

    @Override
    public ColumnCollection<ENTITY, ENTITYBUILDER> getColumnCollection() {
        return columnCollection;
    }

    @Override
    public Function<ENTITYBUILDER, ENTITY> buildFunction() {
        return daoBuilderHelper.getBuildFunction();
    }

    public BUILDER withStringColumn(String columnName,
                                    Function<ENTITY, String> getter,
                                    BiConsumer<ENTITYBUILDER, String> setter){
        Column<?,?,ENTITY, ENTITYBUILDER> column = DataColumnFactory.stringColumn(columnName, daoBuilderHelper.getPrefix(), getter, setter, true);
        columnCollection.addDataColumn(column);
        return (BUILDER) this;
    }

    /**
     * Describes a numeric data element with no decimal or fractional part.
     *
     * @param columnName The name of the column that holds the data element.
     * @param getter The function on <code>ENTITY</code> that returns the data element.
     * @param setter The function on <code>ENTITY</code> that consumes the data element.
     * @return This instance.
     */
    public BUILDER withLongColumn(String columnName,
                                  Function<ENTITY, Long> getter,
                                  BiConsumer<ENTITYBUILDER, Long> setter){
        Column<?,?,ENTITY, ENTITYBUILDER> column = DataColumnFactory.longColumn(columnName, daoBuilderHelper.getPrefix(), getter, setter, true);
        columnCollection.addDataColumn(column);
        return (BUILDER) this;
    }

    /**
     * Describes a numeric data element with a decimal part.
     *
     * @param columnName The name of the column that holds the data element.
     * @param getter The function on <code>ENTITY</code> that returns the data element.
     * @param setter The function on <code>ENTITY</code> that consumes the data element.
     * @return This instance.
     */
    public BUILDER withBigDecimalColumn(String columnName,
                                        Function<ENTITY, BigDecimal> getter,
                                        BiConsumer<ENTITYBUILDER, BigDecimal> setter){
        Column<?,?,ENTITY, ENTITYBUILDER> column = DataColumnFactory.bigDecimalColumn(columnName, daoBuilderHelper.getPrefix(), getter, setter, true);
        columnCollection.addDataColumn(column);
        return (BUILDER) this;
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
    public <E> BUILDER withConvertingStringColumn(String columnName,
                                                  Function<ENTITY, E> getter,
                                                  BiConsumer<ENTITYBUILDER, E> setter,
                                                  Converter<E, String> converter){
        Column<?,?,ENTITY, ENTITYBUILDER> column = DataColumnFactory.stringConverterColumn(columnName, daoBuilderHelper.getPrefix(), getter, setter, converter, true);
        columnCollection.addDataColumn(column);
        return (BUILDER) this;
    }

    /**
     * Describes a data element that represents a time stamp.
     *
     * @param columnName The name of the column that holds the data element.
     * @param getter The function on <code>ENTITY</code> that returns the data element.
     * @param setter The function on <code>ENTITY</code> that consumes the data element.
     * @return This instance.
     */
    public BUILDER withInstantColumn(String columnName,
                                     Function<ENTITY, Instant> getter,
                                     BiConsumer<ENTITYBUILDER, Instant> setter){
        Column<?,?,ENTITY, ENTITYBUILDER> column = DataColumnFactory.instantColumn(columnName, daoBuilderHelper.getPrefix(), getter, setter, true);
        columnCollection.addDataColumn(column);
        return (BUILDER) this;
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
    public BUILDER withBooleanColumn(String columnName,
                                     Function<ENTITY, Boolean> getter,
                                     BiConsumer<ENTITYBUILDER, Boolean> setter){
        Column<?,?,ENTITY, ENTITYBUILDER> column = DataColumnFactory.booleanColumn(columnName, daoBuilderHelper.getPrefix(), getter, setter, true);
        columnCollection.addDataColumn(column);
        return (BUILDER) this;
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
    public BUILDER withStringBooleanColumn(String columnName,
                                           Function<ENTITY, Boolean> getter,
                                           BiConsumer<ENTITYBUILDER, Boolean> setter){
        Column<?,?,ENTITY, ENTITYBUILDER> column = DataColumnFactory.textBackedBooleanColumn(columnName, daoBuilderHelper.getPrefix(), getter, setter, true);
        columnCollection.addDataColumn(column);
        return (BUILDER) this;
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
    public BUILDER withIntegerBooleanColumn(String columnName,
                                            Function<ENTITY, Boolean> getter,
                                            BiConsumer<ENTITYBUILDER, Boolean> setter){
        Column<?,?,ENTITY, ENTITYBUILDER> column = DataColumnFactory.longBackedBooleanColumn(columnName, daoBuilderHelper.getPrefix(), getter, setter,true);
        columnCollection.addDataColumn(column);
        return (BUILDER) this;
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
    public <U> BUILDER withJoinColumn(
            String columnName, Function<ENTITY, U> getter, BiConsumer<ENTITYBUILDER,U> setter, DaoDescriptor<U,?> daoDescriptor){
        JoinColumn<ENTITY,U, ENTITYBUILDER,?> joinColumn = new JoinColumn<>(columnName, daoBuilderHelper.getPrefix(), daoBuilderHelper.getPrefixer(), getter, setter, daoDescriptor, true);
        columnCollection.addJoinColumn(joinColumn);
        return (BUILDER) this;
    }

    /**
     * Sets the most recent column added to this DaoBuilder to prevent it allowing
     * nulls on inserts or updates.
     *
     * @return This instance.
     */
    public BUILDER notNull(){
        columnCollection.setLastColumnAddedNotNull();
        return (BUILDER) this;
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
    public BUILDER setSqlTypeName(String sqlTypeName){
        columnCollection.setLastColumnSqlTypeName(sqlTypeName);
        return (BUILDER) this;
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
    public <T> BUILDER withGenericColumn(String columnName,
                                         Function<ENTITY, T> getter,
                                         BiConsumer<ENTITYBUILDER, T> setter,
                                         GenericColumn<T> genericColumn){
        Column<?,?,ENTITY, ENTITYBUILDER> column = DataColumnFactory.genericColumn(columnName, daoBuilderHelper.getPrefix(), getter, setter, genericColumn, true);
        columnCollection.addDataColumn(column);
        return (BUILDER) this;
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
    public <T,U> BUILDER withConvertedGenericColumn(String columnName,
                                                    Function<ENTITY, U> getter,
                                                    BiConsumer<ENTITYBUILDER, U> setter,
                                                    GenericColumn<T> genericColumn,
                                                    Converter<U,T> converter){
        Column<?,?,ENTITY, ENTITYBUILDER> column = DataColumnFactory.convertedGenericColumn(columnName, daoBuilderHelper.getPrefix(), getter, setter, genericColumn, converter, true);
        columnCollection.addDataColumn(column);
        return (BUILDER) this;
    }

    /**
     * Describe a unique constraint on this entity. At run-time, hrorm makes
     * no use of this information, but it will be used during schema generation.
     * {@link Schema}.
     *
     * @param columnNames the names of the columns that are to be unique
     * @return This instance.
     */
    public BUILDER withUniqueConstraint(String ... columnNames){
        columnCollection.addUniquenConstraint(columnNames);
        return (BUILDER) this;
    }

}
