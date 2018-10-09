package org.hrorm;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Describes a column with a text value that can be mapped
 * to a <code>String</code> and then converted to type <code>E</code>
 *
 * <p>
 *
 * The primary intention of this column type is to support object
 * models that contain either enumerated types or thing wrappers
 * around <code>String</code>s
 *
 * <p>
 *
 * Most users of hrorm will have no need to directly use this.
 *
 * @param <T> The entity type this column belongs to
 * @param <E> The type that this column is mapped to
 */
public class StringConverterColumn<T, E> implements TypedColumn<T> {

    private final String name;
    private final String prefix;
    private final BiConsumer<T, E> setter;
    private final Function<T, E> getter;
    private final Converter<E, String> converter;

    public StringConverterColumn(String name, String prefix, Function<T, E> getter, BiConsumer<T, E> setter, Converter<E, String> converter) {
        this.name = name;
        this.prefix = prefix;
        this.getter = getter;
        this.setter = setter;
        this.converter = converter;
    }

    @Override
    public TypedColumn<T> withPrefix(String prefix) {
        return new StringConverterColumn<>(name, prefix, getter, setter, converter);
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
    public PopulateResult populate(T item, ResultSet resultSet) throws SQLException {
        String code = resultSet.getString(prefix + name);
        E value = null;
        if (code != null ) {
            value = converter.to(code);
        }
        setter.accept(item, value);
        return PopulateResult.Ignore;
    }

    @Override
    public void setValue(T item, int index, PreparedStatement preparedStatement) throws SQLException {
        E value = getter.apply(item);
        String code = null;
        if (value != null ) {
            code = converter.from(value);
        }
        preparedStatement.setString(index, code);
    }

    @Override
    public boolean isPrimaryKey() {
        return false;
    }

}
