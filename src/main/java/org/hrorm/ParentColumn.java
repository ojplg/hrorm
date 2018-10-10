package org.hrorm;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ParentColumn<T, P> implements TypedColumn<T> {

    private final String name;
    private final String prefix;
    private final BiConsumer<T, P> setter;
    private final Function<T, P> getter;
    private PrimaryKey<P> parentPrimaryKey;
    private boolean nullable;

    public ParentColumn(String name, String prefix, Function<T, P> getter, BiConsumer<T, P> setter) {
        this.name = name;
        this.prefix = prefix;
        this.getter = getter;
        this.setter = setter;
        this.nullable = false;
    }

    public ParentColumn(String name, String prefix, Function<T, P> getter, BiConsumer<T, P> setter, PrimaryKey<P> parentPrimaryKey, boolean nullable) {
        this.name = name;
        this.prefix = prefix;
        this.getter = getter;
        this.setter = setter;
        this.nullable = nullable;
        this.parentPrimaryKey = parentPrimaryKey;
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
        return PopulateResult.ParentColumn;
    }

    @Override
    public void setValue(T item, int index, PreparedStatement preparedStatement) throws SQLException {
        P parent = getter.apply(item);
        Long parentId = parentPrimaryKey.getKey(parent);
        if ( parentId == null ){
            if ( nullable ){
                preparedStatement.setNull(index, Types.INTEGER);
            } else {
                throw new HrormException("Tried to set a null value for " + prefix + "." + name + " which was set not nullable.");
            }
        } else {
            preparedStatement.setLong(index, parentId);
        }

    }

    @Override
    public TypedColumn<T> withPrefix(String prefix) {
        return new ParentColumn<>(name, prefix, getter, setter, parentPrimaryKey, nullable);
    }

    @Override
    public boolean isPrimaryKey() {
        return false;
    }

    @Override
    public void notNull() {
        this.nullable = false;
    }

    public BiConsumer<T, P> setter(){
        return setter;
    }

    public void setParentPrimaryKey(PrimaryKey<P> parentPrimaryKey) {
        this.parentPrimaryKey = parentPrimaryKey;
    }
}
