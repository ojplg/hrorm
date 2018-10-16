package org.hrorm;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Describes a column with an date/time value that can be mapped
 * to a <code>LocalDateTime</code>
 *
 * <p>
 *
 * Most users of hrorm will have no need to directly use this.
 *
 * @param <T> The entity type this column belongs to
 */
public class LocalDateTimeColumn<T> implements IndirectTypedColumn<T,T> {

    private final String name;
    private final String prefix;
    private final BiConsumer<T, LocalDateTime> setter;
    private final Function<T, LocalDateTime> getter;
    private boolean nullable = true;

    public LocalDateTimeColumn(String name, String prefix, Function<T, LocalDateTime> getter, BiConsumer<T, LocalDateTime> setter) {
        this.name = name;
        this.prefix = prefix;
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public IndirectTypedColumn<T,T> withPrefix(String prefix, Prefixer prefixer) {
        LocalDateTimeColumn column = new LocalDateTimeColumn<>(name, prefix, getter, setter);
        if ( ! nullable ){
            column.notNull();
        }
        return column;
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
        Timestamp sqlTime = resultSet.getTimestamp(prefix + name);
        LocalDateTime value = null;
        if ( sqlTime != null ) {
            value = sqlTime.toLocalDateTime();
        }
        setter.accept(item, value);
        return PopulateResult.Ignore;
    }

    @Override
    public void setValue(T item, int index, PreparedStatement preparedStatement) throws SQLException {
        LocalDateTime value = getter.apply(item);
        Timestamp sqlTime = null;
        if ( value == null) {
            if ( ! nullable ){
                throw new HrormException("Tried to set a null value for " + prefix + "." + name + " which was set not nullable.");
            }
        } else {
            sqlTime = Timestamp.valueOf(value);
        }
        preparedStatement.setTimestamp(index, sqlTime);
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



























