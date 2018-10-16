package org.hrorm;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ConstructingDaoBuilder<T,CONSTRUCTOR>  {

    private final Function<CONSTRUCTOR, T> construct;

    private final Prefixer prefixer;
    private final String myPrefix;

    public ConstructingDaoBuilder(String tableName, Supplier<CONSTRUCTOR> constructor, Function<CONSTRUCTOR, T> construct){
        this.construct = construct;
        this.prefixer = new Prefixer();
        this.myPrefix = this.prefixer.nextPrefix();
    }

    /**
     * Set data about the primary key of the table for this type. Hrorm demands that primary keys be
     * sequence numbers from the database. GUIDs and other constructions are not allowed. All
     * Daos must have a primary key.
     *
     * @param columnName The name of the column in the table that holds the primary key.
     * @param sequenceName The name of the sequence that will provide new keys.
     * @param getter The function to call to get the primary key value from an object instance.
     * @param setter The function to call to set the primary key value to the object's builder instance.
     * @return This instance.
     */
    public ConstructingDaoBuilder<T,CONSTRUCTOR> withPrimaryKey(String columnName, String sequenceName, Function<T, Long> getter, BiConsumer<CONSTRUCTOR, Long> setter){
        internalDaoBuilder.withPrimaryKey(columnName,sequenceName,wrap(getter),setter);
        return this;
    }

    /**
     * Describes a text or string data element.
     *
     * @param columnName The name of the column that holds the data element.
     * @param getter The function on <code>T</code> that returns the data element.
     * @param setter The function on <code>T</code> that consumes the data element.
     * @return This instance.
     */
    public ConstructingDaoBuilder<T,CONSTRUCTOR> withStringColumn(String columnName, Function<T, String> getter, BiConsumer<CONSTRUCTOR, String> setter){
        internalDaoBuilder.withStringColumn(columnName, wrap(getter), setter);
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
    public ConstructingDaoBuilder<T,CONSTRUCTOR> withBigDecimalColumn(String columnName, Function<T, BigDecimal> getter, BiConsumer<CONSTRUCTOR, BigDecimal> setter){
        internalDaoBuilder.withBigDecimalColumn(columnName, wrap(getter), setter);
        return this;
    }


    /**
     * Creates a {@link Dao} for performing CRUD operations of type <code>T</code>.
     *
     * @param connection The SQL connection this <code>Dao</code> will use
     *                   for its operations.
     * @return The newly created <code>Dao</code>.
     */
    public Dao<T> buildDao(Connection connection){

        Dao<CONSTRUCTOR> constructorDao = new DaoImpl<>(connection,
                internalDaoBuilder.tableName(),
                internalDaoBuilder.supplier(),
                internalDaoBuilder.primaryKey(),
                internalDaoBuilder.dataColumns(),
                internalDaoBuilder.joinColumns(),
                internalDaoBuilder.childrenDescriptors(),
                internalDaoBuilder.parentColumn());


        return null;
    }


    private <X> Function<CONSTRUCTOR, X> wrap(Function<T, X> getter){
        return constructor -> {
            T item = construct.apply(constructor);
            return getter.apply(item);
        };
    }

}
