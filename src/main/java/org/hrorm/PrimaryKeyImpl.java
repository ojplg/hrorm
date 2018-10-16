package org.hrorm;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class PrimaryKeyImpl<T> implements IndirectTypedColumn<T,T> {

    private final LongColumn<T> longColumn;
    private final Function<T, Long> getter;
    private final BiConsumer<T, Long> setter;
    private final String sequenceName;

    public PrimaryKeyImpl(String name, String prefix, Function<T, Long> getter, BiConsumer<T, Long> setter, String sequenceName) {
        this.getter = getter;
        this.setter = setter;
        this.sequenceName = sequenceName;
        this.longColumn = new LongColumn<>(name, prefix, getter, setter, false);
    }

    public Long getKey(T item) {
        return getter.apply(item);
    }

    public String getSequenceName() {
        return sequenceName;
    }

    public void setKey(T item, Long id) {
        setter.accept(item, id);
    }

    @Override
    public IndirectTypedColumn<T,T> withPrefix(String prefix, Prefixer prefixer) {
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
    public PopulateResult populate(T item, ResultSet resultSet) throws SQLException {
        longColumn.populate(item, resultSet);
        Long primaryKeyValue = getter.apply(item);
        if (primaryKeyValue == null || primaryKeyValue == 0 ){
            return PopulateResult.NoPrimaryKey;
        }
        return PopulateResult.PrimaryKey;
    }

    @Override
    public void setValue(T item, int index, PreparedStatement preparedStatement) throws SQLException {
        longColumn.setValue(item, index, preparedStatement);
    }

    @Override
    public boolean isPrimaryKey() {
        return true;
    }

    @Override
    public void notNull() {
        // nullability is never allowed, this does nothing
    }

}
