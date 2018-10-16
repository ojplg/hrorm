package org.hrorm;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ImmutableObjectPrimaryKey<T, CONSTRUCTOR> implements IndirectPrimaryKey<T, CONSTRUCTOR>, IndirectTypedColumn<T, CONSTRUCTOR> {

    private final String prefix;
    private final String name;
    private final String sequenceName;
    private final BiConsumer<CONSTRUCTOR, Long> setter;
    private final Function<T, Long> getter;

    public ImmutableObjectPrimaryKey(String prefix,
                                     String name,
                                     String sequenceName,
                                     Function<T, Long> getter,
                                     BiConsumer<CONSTRUCTOR, Long> setter) {
        this.prefix = prefix;
        this.name = name;
        this.sequenceName = sequenceName;
        this.setter = setter;
        this.getter = getter;
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
    public void setKey(CONSTRUCTOR builder, Long id) {
        setter.accept(builder, id);
    }

    @Override
    public void optimisticSetKey(T item, Long id) {
        // FIXME: This is awful!!
        try {
            CONSTRUCTOR constructor = (CONSTRUCTOR) item;
            setter.accept(constructor, id);
        } catch (ClassCastException ex){
            System.out.println(ex);
        }
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
    public IndirectTypedColumn<T, CONSTRUCTOR> withPrefix(String newPrefix, Prefixer prefixer) {
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
