package org.hrorm;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class IndirectDaoBuilder<ENTITY, ENTITYBUILDER>  {

    private final String tableName;
    private final Supplier<ENTITYBUILDER> entityBuilder;
    private final Function<ENTITYBUILDER, ENTITY> build;
    private final Prefixer prefixer;
    private final String myPrefix;

    private PrimaryKeyImpl<ENTITY, ENTITYBUILDER> primaryKey;

    private List<IndirectTypedColumn<ENTITY, ENTITYBUILDER>> dataColumns = new ArrayList<>();

    public IndirectDaoBuilder(String tableName, Supplier<ENTITYBUILDER> entityBuilder, Function<ENTITYBUILDER, ENTITY> build){
        this.tableName = tableName;
        this.entityBuilder = entityBuilder;
        this.build = build;
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
    public IndirectDaoBuilder<ENTITY, ENTITYBUILDER> withPrimaryKey(String columnName, String sequenceName, Function<ENTITY, Long> getter, BiConsumer<ENTITYBUILDER, Long> setter){
        primaryKey = new PrimaryKeyImpl<>(myPrefix, columnName, sequenceName, getter, setter);
        dataColumns.add(primaryKey);
        return this;
    }

    /**
     * Describes a text or string data element.
     *
     * @param columnName The name of the column that holds the data element.
     * @param getter The function on <code>ENTITY</code> that returns the data element.
     * @param setter The function on <code>ENTITY</code> that consumes the data element.
     * @return This instance.
     */
    public IndirectDaoBuilder<ENTITY, ENTITYBUILDER> withStringColumn(String columnName, Function<ENTITY, String> getter, BiConsumer<ENTITYBUILDER, String> setter){
        StringColumn<ENTITY, ENTITYBUILDER> stringColumn =
                new StringColumn<>(columnName, myPrefix, getter, setter, true);
        dataColumns.add(stringColumn);
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
    public IndirectDaoBuilder<ENTITY, ENTITYBUILDER> withBigDecimalColumn(String columnName, Function<ENTITY, BigDecimal> getter, BiConsumer<ENTITYBUILDER, BigDecimal> setter){
        BigDecimalColumn<ENTITY, ENTITYBUILDER> column =
                new BigDecimalColumn<>(columnName, myPrefix, getter, setter, true);
        dataColumns.add(column);
        return this;
    }

    /**
     * Creates a {@link Dao} for performing CRUD operations of type <code>ENTITY</code>.
     *
     * @param connection The SQL connection this <code>Dao</code> will use
     *                   for its operations.
     * @return The newly created <code>Dao</code>.
     */
    public Dao<ENTITY> buildDao(Connection connection){

        return new DaoImpl<>(
                connection,
                tableName,
                entityBuilder,
                primaryKey,
                dataColumns,
                Collections.emptyList(),
                Collections.emptyList(),
                null,
                build);
    }

    private <X> Function<ENTITYBUILDER, X> wrap(Function<ENTITY, X> getter){
        return constructor -> {
            ENTITY item = build.apply(constructor);
            return getter.apply(item);
        };
    }

}
