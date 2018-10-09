package org.hrorm;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Describes a column with an integer value that can be mapped
 * to a <code>Long</code>
 *
 * <p>
 *
 * Most users of hrorm will have no need to directly use this.
 *
 * @param <T> The entity type this column belongs to
 */
public class LongColumn<T> implements TypedColumn<T> {

    private final String name;
    private final String prefix;
    private final BiConsumer<T, Long> setter;
    private final Function<T, Long> getter;
    private boolean nullable;

    public LongColumn(String name, String prefix, Function<T, Long> getter, BiConsumer<T, Long> setter) {
        this(name, prefix, getter, setter, true);
    }

    public LongColumn(String name, String prefix, Function<T, Long> getter, BiConsumer<T, Long> setter, boolean nullable) {
        this.name = name;
        this.prefix = prefix;
        this.getter = getter;
        this.setter = setter;
        this.nullable = nullable;
    }

    @Override
    public TypedColumn<T> withPrefix(String prefix) {
        return new LongColumn<>(name, prefix, getter, setter, nullable);
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
        Long value = resultSet.getLong(prefix  + name);
        setter.accept(item, value);
        return PopulateResult.Ignore;
    }

    @Override
    public void setValue(T item, int index, PreparedStatement preparedStatement) throws SQLException {
        Long value = getter.apply(item);
        if ( value == null ){
            if ( nullable ){
                preparedStatement.setNull(index, Types.INTEGER);
            } else {
                throw new HrormException("Tried to set a null value for " + prefix + "." + name + " which was set not nullable.");
            }
        } else {
            preparedStatement.setLong(index, value);
        }
    }

    @Override
    public boolean isPrimaryKey() {
        return false;
    }

    public void notNull(){
        nullable=false;
    }
}
