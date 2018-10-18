package org.hrorm;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class DirectPrimaryKey<ENTITY> implements PrimaryKey<ENTITY, ENTITY> {

    private final String prefix;
    private final String name;
    private final String sequenceName;
    private final BiConsumer<ENTITY, Long> setter;
    private final Function<ENTITY, Long> getter;

    public DirectPrimaryKey(String prefix,
                            String name,
                            String sequenceName,
                            Function<ENTITY, Long> getter,
                            BiConsumer<ENTITY, Long> setter) {
        this.prefix = prefix;
        this.name = name;
        this.sequenceName = sequenceName;
        this.setter = setter;
        this.getter = getter;
    }

    @Override
    public Long getKey(ENTITY item) {
        if ( item == null ){
            throw new HrormException("Cannot get a key from a null item ");
        }
        return getter.apply(item);
    }

    @Override
    public String getSequenceName() {
        return sequenceName;
    }

    @Override
    public void setKey(ENTITY builder, Long id) {
        setter.accept(builder, id);
    }

    @Override
    public void optimisticSetKey(ENTITY item, Long id) {
        setter.accept(item, id);
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
    public PopulateResult populate(ENTITY constructor, ResultSet resultSet) throws SQLException {
        Long value = resultSet.getLong(prefix  + name);
        setter.accept(constructor, value);
        if (value == null || value == 0 ){
            return PopulateResult.NoPrimaryKey;
        }
        return PopulateResult.PrimaryKey;

    }

    @Override
    public void setValue(ENTITY item, int index, PreparedStatement preparedStatement) throws SQLException {
        Long value = getter.apply(item);
        if ( value == null ){
            throw new HrormException("Tried to set a null value for the primary key named " + name);
        } else {
            preparedStatement.setLong(index, value);
        }
    }

    @Override
    public Column<ENTITY, ENTITY> withPrefix(String newPrefix, Prefixer prefixer) {
        return new DirectPrimaryKey<>(newPrefix, name, sequenceName, getter, setter);
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
