package org.hrorm;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class DaoBuilder<T> implements DaoDescriptor<T> {

    private final String tableName;
    private final List<TypedColumn<T>> columns = new ArrayList<>();
    private final List<JoinColumn<T,?>> joinColumns = new ArrayList<>();
    private final List<ChildrenDescriptor<T,?>> childrenDescriptors = new ArrayList<>();
    private PrimaryKey<T> primaryKey;
    private final Supplier<T> supplier;
    private int prefixIndex = 0;
    private String[] prefixes = new String[] {"a","b","c","d","e","f","g","h","i","j","k","l"};

    public DaoBuilder(String tableName, Supplier<T> supplier){
        this.tableName = tableName;
        this.supplier = supplier;
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

    public Dao<T> buildDao(Connection connection){
        return new DaoImpl<>(connection, tableName, supplier, primaryKey, columns, joinColumns, childrenDescriptors);
    }

    public DaoBuilder<T> withStringColumn(String columnName, Function<T, String> getter, BiConsumer<T, String> setter){
        TypedColumn<T> column = new StringColumn<>(columnName, "a", getter, setter);
        columns.add(column);
        return this;
    }

    public DaoBuilder<T> withIntegerColumn(String columnName, Function<T, Long> getter, BiConsumer<T, Long> setter){
        TypedColumn<T> column = new LongColumn<>(columnName, "a", getter, setter);
        columns.add(column);
        return this;
    }

    public <E> DaoBuilder<T> withConvertingStringColumn(String columnName, Function<T, E> getter, BiConsumer<T, E> setter, Converter<E, String> converter){
        TypedColumn<T> column = new StringConverterColumn<>(columnName, "a", getter, setter, converter);
        columns.add(column);
        return this;
    }

    public DaoBuilder<T> withLocalDateTimeColumn(String columnName, Function<T, LocalDateTime> getter, BiConsumer<T, LocalDateTime> setter){
        columns.add(new LocalDateTimeColumn<>(columnName, "a", getter, setter));
        return this;
    }

    public DaoBuilder<T> withBooleanColumn(String columnName, Function<T, Boolean> getter, BiConsumer<T, Boolean> setter){
        columns.add(new StringConverterColumn<>(columnName, "a", getter, setter, BooleanConverter.INSTANCE));
        return this;
    }

    public <U> DaoBuilder<T> withJoinColumn(String columnName, Function<T, U> getter, BiConsumer<T,U> setter, DaoDescriptor<U> daoDescriptor){
        prefixIndex += 1;
        JoinColumn<T,U> joinColumn = new JoinColumn<>(columnName, prefixes[prefixIndex], getter, setter, daoDescriptor);
        joinColumns.add(joinColumn);
        return this;
    }

    public <U> DaoBuilder<T> withChildren(String parentChildColumnName, BiConsumer<U,Long> parentSetter,
                                          Function<T, List<U>> getter, BiConsumer<T, List<U>> setter, DaoDescriptor<U> daoDescriptor){
        childrenDescriptors.add(
                new ChildrenDescriptor<>(parentChildColumnName, parentSetter, getter, setter, daoDescriptor, primaryKey)
        );
        return this;
    }

    public DaoBuilder<T> withPrimaryKey(String columnName, String sequenceName, Function<T, Long> getter, BiConsumer<T, Long> setter){
        this.primaryKey = new PrimaryKeyImpl<>(columnName, "a", getter, setter, sequenceName);
        columns.add(primaryKey);
        return this;
    }

}
