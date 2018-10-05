package org.hrorm;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class PrimaryKeyImpl<T> implements PrimaryKey<T> {

    private final LongColumn<T> longColumn;
    private final Function<T, Long> getter;
    private final BiConsumer<T, Long> setter;
    private final String sequenceName;

    public PrimaryKeyImpl(String name, String prefix, Function<T, Long> getter, BiConsumer<T, Long> setter, String sequenceName) {
        this.getter = getter;
        this.setter = setter;
        this.sequenceName = sequenceName;
        this.longColumn = new LongColumn<>(name, prefix, getter, setter);
    }

    @Override
    public Long getKey(T item) {
        return getter.apply(item);
    }

    @Override
    public String getSequenceName() {
        return sequenceName;
    }

    @Override
    public void setKey(T item, Long id) {
        setter.accept(item, id);
    }

    @Override
    public TypedColumn<T> withPrefix(String prefix) {
        return new PrimaryKeyImpl<>(longColumn.getName(), prefix, getter, setter, sequenceName);
    }

    @Override
    public String getName() {
        return longColumn.getName();
    }

    @Override
    public String getPrefix() {
        return longColumn.getPrefix();
    }

    @Override
    public void populate(T item, ResultSet resultSet) throws SQLException {
        longColumn.populate(item, resultSet);
    }

    @Override
    public void setValue(T item, int index, PreparedStatement preparedStatement) throws SQLException {
        longColumn.setValue(item, index, preparedStatement);
    }

    @Override
    public boolean isPrimaryKey() {
        return true;
    }
}
