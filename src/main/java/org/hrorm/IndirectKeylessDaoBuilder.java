package org.hrorm;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.Instant;
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
 *    and these entities cannot be joined to other objects or have children or be children to other objects.
 *    In general, the regular variants should be preferred.
 * </p>
 *
 * @param <ENTITY> The class that the <code>KeylessDao</code> will support.
 * @param <BUILDER> The class that builds the <code>ENTITY</code> type.
 */
public class IndirectKeylessDaoBuilder<ENTITY, BUILDER> implements KeylessDaoDescriptor<ENTITY, BUILDER> {


    private final ColumnCollection<ENTITY, BUILDER> columnCollection = new ColumnCollection<>();
    private final DaoBuilderHelper<ENTITY, BUILDER> daoBuilderHelper;

    public IndirectKeylessDaoBuilder(String table, Supplier<BUILDER> supplier, Function<BUILDER, ENTITY> buildFunction) {
        this.daoBuilderHelper = new DaoBuilderHelper(table, supplier, buildFunction);
    }

    @Override
    public String tableName() {
        return daoBuilderHelper.getTableName();
    }

    @Override
    public Supplier<BUILDER> supplier() {
        return daoBuilderHelper.getSupplier();
    }

    @Override
    public ColumnCollection<ENTITY, BUILDER> getColumnCollection() {
        return columnCollection;
    }

    @Override
    public Function<BUILDER, ENTITY> buildFunction() {
        return daoBuilderHelper.getBuildFunction();
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
        Column<ENTITY, BUILDER> column = DataColumnFactory.stringColumn(columnName, daoBuilderHelper.getPrefix(), getter, setter, true);
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
    public IndirectKeylessDaoBuilder<ENTITY, BUILDER> withLongColumn(String columnName, Function<ENTITY, Long> getter, BiConsumer<BUILDER, Long> setter){
        Column<ENTITY, BUILDER> column = DataColumnFactory.longColumn(columnName, daoBuilderHelper.getPrefix(), getter, setter, true);
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
    public IndirectKeylessDaoBuilder<ENTITY, BUILDER> withBigDecimalColumn(String columnName, Function<ENTITY, BigDecimal> getter, BiConsumer<BUILDER, BigDecimal> setter){
        Column<ENTITY, BUILDER> column = DataColumnFactory.bigDecimalColumn(columnName, daoBuilderHelper.getPrefix(), getter, setter, true);
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
    public <E> IndirectKeylessDaoBuilder<ENTITY, BUILDER> withConvertingStringColumn(String columnName, Function<ENTITY, E> getter, BiConsumer<BUILDER, E> setter, Converter<E, String> converter){
        Column<ENTITY, BUILDER> column = DataColumnFactory.stringConverterColumn(columnName, daoBuilderHelper.getPrefix(), getter, setter, converter, true);
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
    public IndirectKeylessDaoBuilder<ENTITY, BUILDER> withInstantColumn(String columnName, Function<ENTITY, Instant> getter, BiConsumer<BUILDER, Instant> setter){
        Column<ENTITY, BUILDER> column = DataColumnFactory.instantColumn(columnName, daoBuilderHelper.getPrefix(), getter, setter, true);
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
    public IndirectKeylessDaoBuilder<ENTITY, BUILDER> withBooleanColumn(String columnName, Function<ENTITY, Boolean> getter, BiConsumer<BUILDER, Boolean> setter){
        Column<ENTITY, BUILDER> column = DataColumnFactory.booleanColumn(columnName, daoBuilderHelper.getPrefix(), getter, setter, true);
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
    public IndirectKeylessDaoBuilder<ENTITY, BUILDER> withStringBooleanColumn(String columnName, Function<ENTITY, Boolean> getter, BiConsumer<BUILDER, Boolean> setter){
        Column<ENTITY, BUILDER> column = DataColumnFactory.textBackedBooleanColumn(columnName, daoBuilderHelper.getPrefix(), getter, setter, true);
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
    public IndirectKeylessDaoBuilder<ENTITY, BUILDER> withIntegerBooleanColumn(String columnName, Function<ENTITY, Boolean> getter, BiConsumer<BUILDER, Boolean> setter){
        Column<ENTITY, BUILDER> column = DataColumnFactory.longBackedBooleanColumn(columnName, daoBuilderHelper.getPrefix(), getter, setter, true);
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
    public <U> IndirectKeylessDaoBuilder<ENTITY, BUILDER> withJoinColumn(String columnName, Function<ENTITY, U> getter, BiConsumer<BUILDER,U> setter, DaoDescriptor<U,?> daoDescriptor){
        JoinColumn<ENTITY,U, BUILDER,?> joinColumn = new JoinColumn<>(columnName, daoBuilderHelper.getPrefix(), daoBuilderHelper.getPrefixer(), getter, setter, daoDescriptor, true);
        columnCollection.addJoinColumn(joinColumn);
        return this;
    }

    /**
     * Sets the most recent column added to this DaoBuilder to prevent it allowing
     * nulls on inserts or updates.
     *
     * @return This instance.
     */
    public IndirectKeylessDaoBuilder<ENTITY, BUILDER> notNull(){
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
    public IndirectKeylessDaoBuilder<ENTITY, BUILDER> setSqlTypeName(String sqlTypeName){
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
    public <T> IndirectKeylessDaoBuilder<ENTITY, BUILDER> withGenericColumn(String columnName,
                                                                     Function<ENTITY, T> getter,
                                                                     BiConsumer<BUILDER, T> setter,
                                                                     GenericColumn<T> genericColumn){
        Column<ENTITY, BUILDER> column = DataColumnFactory.genericColumn(columnName, daoBuilderHelper.getPrefix(), getter, setter, genericColumn, true);
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
    public <T,U> IndirectKeylessDaoBuilder<ENTITY, BUILDER> withConvertedGenericColumn(String columnName,
                                                                                Function<ENTITY, U> getter,
                                                                                BiConsumer<BUILDER, U> setter,
                                                                                GenericColumn<T> genericColumn,
                                                                                Converter<U,T> converter){
        Column<ENTITY, BUILDER> column = DataColumnFactory.convertedGenericColumn(columnName, daoBuilderHelper.getPrefix(), getter, setter, genericColumn, converter, true);
        columnCollection.addDataColumn(column);
        return this;
    }

}
