package org.hrorm;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class StringColumn<T> implements TypedColumn<T> {

    private final String name;
    private final String prefix;
    private final BiConsumer<T, String> setter;
    private final Function<T, String> getter;

    public StringColumn(String name, String prefix, Function<T, String> getter, BiConsumer<T, String> setter) {
        this.name = name;
        this.prefix = prefix;
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public TypedColumn<T> withPrefix(String prefix) {
        return new StringColumn<>(name, prefix, getter, setter);
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
        String value = resultSet.getString(prefix + name);
        setter.accept(item, value);
    }

    @Override
    public void setValue(T item, int index, PreparedStatement preparedStatement) throws SQLException {
        String value = getter.apply(item);
        preparedStatement.setString(index, value);
    }

    @Override
    public boolean isPrimaryKey() {
        return false;
    }

}
