package org.hrorm;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Describes a column with a text value that can be mapped
 * to a <code>String</code>
 *
 * <p>
 *
 * Most users of hrorm will have no need to directly use this.
 *
 * @param <T> The entity type this column belongs to
 */
public class StringColumn<T> implements IndirectTypedColumn<T,T> {

    private final String name;
    private final String prefix;
    private final BiConsumer<T, String> setter;
    private final Function<T, String> getter;
    private boolean nullable;

    public StringColumn(String name, String prefix, Function<T, String> getter, BiConsumer<T, String> setter, boolean nullable) {
        this.name = name;
        this.prefix = prefix;
        this.getter = getter;
        this.setter = setter;
        this.nullable = nullable;
    }

    public StringColumn(String name, String prefix, Function<T, String> getter, BiConsumer<T, String> setter) {
        this(name, prefix, getter, setter, true);
    }


    @Override
    public IndirectTypedColumn<T,T> withPrefix(String prefix, Prefixer prefixer) {
        return new StringColumn<>(name, prefix, getter, setter, nullable);
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
        String value = resultSet.getString(prefix + name);
        setter.accept(item, value);
        return PopulateResult.Ignore;
    }

    @Override
    public void setValue(T item, int index, PreparedStatement preparedStatement) throws SQLException {
        String value = getter.apply(item);
        if( value == null && ! nullable ){
            throw new HrormException("Tried to set a null value for " + prefix + "." + name + " which was set not nullable.");
        }
        preparedStatement.setString(index, value);
    }

    @Override
    public boolean isPrimaryKey() {
        return false;
    }

    @Override
    public void notNull() {
        nullable = false;
    }
}
