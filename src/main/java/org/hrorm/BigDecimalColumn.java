package org.hrorm;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Describes a column with a decimal value that can be mapped
 * to a <code>BigDecimal</code>
 *
 * <p>
 *
 * Most users of hrorm will have no need to directly use this.
 *
 * @param <T> The entity type this column belongs to
 */
public class BigDecimalColumn<T> implements TypedColumn<T> {

    private final String name;
    private final String prefix;
    private final BiConsumer<T, BigDecimal> setter;
    private final Function<T, BigDecimal> getter;

    public BigDecimalColumn(String name, String prefix, Function<T, BigDecimal> getter, BiConsumer<T, BigDecimal> setter) {
        this.name = name;
        this.prefix = prefix;
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public TypedColumn<T> withPrefix(String prefix) {
        return new BigDecimalColumn<>(name, prefix, getter, setter);
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
        BigDecimal value = resultSet.getBigDecimal(prefix  + name);
        setter.accept(item, value);
        return PopulateResult.Ignore;
    }

    @Override
    public void setValue(T item, int index, PreparedStatement preparedStatement) throws SQLException {
        BigDecimal value = getter.apply(item);
        preparedStatement.setBigDecimal(index, value);
    }

    @Override
    public boolean isPrimaryKey() {
        return false;
    }
}
