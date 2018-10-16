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
public class ImmutableObjectStringColumn<T,CONSTRUCTOR> implements IndirectTypedColumn<T, CONSTRUCTOR> {

    private final String name;
    private final String prefix;
    private final BiConsumer<CONSTRUCTOR, String> setter;
    private final Function<T, String> getter;
    private boolean nullable;

    private final Function<CONSTRUCTOR, T> construct;


    public ImmutableObjectStringColumn(String name, String prefix, Function<T, String> getter, BiConsumer<CONSTRUCTOR, String> setter, Function<CONSTRUCTOR, T> construct, boolean nullable) {
        this.name = name;
        this.prefix = prefix;
        this.getter = getter;
        this.setter = setter;
        this.nullable = nullable;
        this.construct = construct;
    }

    @Override
    public TypedColumn<T> withPrefix(String prefix, Prefixer prefixer) {
        return new ImmutableObjectStringColumn<>(name, prefix, getter, setter, construct, nullable);
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
    public PopulateResult populate(CONSTRUCTOR constructor, ResultSet resultSet) throws SQLException {
        String value = resultSet.getString(prefix + name);
        setter.accept(constructor, value);
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
