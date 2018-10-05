package org.hrorm;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class JoinColumn<T, J> implements TypedColumn<T> {

    private final String name;
    private final String prefix;
    private final String table;
    private final BiConsumer<T, J> setter;
    private final Function<T, J> getter;
    private final Supplier<J> supplier;
    private final List<TypedColumn<J>> columnList;
    private final PrimaryKey<J> primaryKey;

    public JoinColumn(String name, String prefix, Function<T, J> getter, BiConsumer<T,J> setter, DaoDescriptor<J> daoDescriptor){
        this.name = name;
        this.prefix = prefix;
        this.getter = getter;
        this.setter = setter;
        this.table = daoDescriptor.tableName();
        this.supplier = daoDescriptor.supplier();
        this.columnList = daoDescriptor.dataColumns().stream().map(c -> c.withPrefix(prefix)).collect(Collectors.toList());
        this.primaryKey = daoDescriptor.primaryKey();
    }

    public JoinColumn(String name, String prefix, String table, Function<T, J> getter, BiConsumer<T, J> setter, Supplier<J> supplier, PrimaryKey<J> primaryKey, List<TypedColumn<J>> columnList) {
        this.name = name;
        this.table = table;
        this.prefix = prefix;
        this.getter = getter;
        this.setter = setter;
        this.supplier = supplier;
        this.primaryKey = primaryKey;
        this.columnList = columnList.stream().map( c -> c.withPrefix(prefix) ).collect(Collectors.toList());
    }

    public String getTable(){
        return table;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public void populate(T item, ResultSet resultSet) throws SQLException {
        J joined  = supplier.get();
        for (TypedColumn<J> column: columnList) {
            column.populate(joined, resultSet);
        }

        setter.accept(item, joined);
    }

    @Override
    public void setValue(T item, int index, PreparedStatement preparedStatement) throws SQLException {
        J value = getter.apply(item);
        if( value == null){
            throw new RuntimeException("Cannot find join column value for " + item + " for " + name);
        }
        Long id = primaryKey.getKey(value);
        preparedStatement.setLong(index, id);
    }

    @Override
    public TypedColumn<T> withPrefix(String prefix) {
        return new JoinColumn<>(name, prefix, table, getter, setter, supplier, primaryKey, columnList);
    }

    public List<TypedColumn<J>> getColumnList(){
        return columnList;
    }

    @Override
    public boolean isPrimaryKey() {
        return false;
    }
}
