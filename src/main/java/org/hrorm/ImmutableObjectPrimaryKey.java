package org.hrorm;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ImmutableObjectPrimaryKey<T, CONSTRUCTOR> implements PrimaryKey<T> {

    private final String prefix;
    private final String name;
    private final String sequenceName;
    private final BiConsumer<CONSTRUCTOR, Long> setter;
    private final Function<T, Long> getter;
    private final Function<CONSTRUCTOR, T> construct;

    private CONSTRUCTOR constructor;


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
        setter.accept(constructor, id);
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
        setter.accept(constructor, value);
        if (value == null || value == 0 ){
            return PopulateResult.NoPrimaryKey;
        }
        return PopulateResult.PrimaryKey;

    }

    @Override
    public void setValue(T item, int index, PreparedStatement preparedStatement) throws SQLException {
        Long value = getter.apply(item);
        if ( value == null ){
            throw new HrormException("Tried to set a null value for the primary key named " + name);
        } else {
            preparedStatement.setLong(index, value);
        }

    }

    @Override
    public TypedColumn<T> withPrefix(String newPrefix, Prefixer prefixer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isPrimaryKey() {
        return true;
    }

    @Override
    public void notNull() {
        throw new HrormException("Cannot set a primary key to be nullable");
    }
}
