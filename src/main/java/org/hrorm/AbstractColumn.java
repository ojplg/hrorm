package org.hrorm;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Represents a column in a database table.
 *
 * @param <TYPE> The Java type that this column should be translated to and from.
 * @param <ENTITY> The entity this column belongs to.
 * @param <BUILDER> The class that is used to build new entity instances.
 */
public abstract class AbstractColumn<TYPE,ENTITY,BUILDER> implements Column<ENTITY,BUILDER> {

    private final String name;
    private final String prefix;
    protected final BiConsumer<BUILDER, TYPE> setter;
    protected final Function<ENTITY, TYPE> getter;
    protected boolean nullable;

    public AbstractColumn(String name, String prefix, Function<ENTITY, TYPE> getter, BiConsumer<BUILDER, TYPE> setter, boolean nullable) {
        this.name = name;
        this.prefix = prefix;
        this.getter = getter;
        this.setter = setter;
        this.nullable = nullable;
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
    public PopulateResult populate(BUILDER builder, ResultSet resultSet) throws SQLException {
        TYPE value = fromResultSet(resultSet, prefix  + name);
        setter.accept(builder, value);
        return PopulateResult.Ignore;
    }

    @Override
    public void setValue(ENTITY item, int index, PreparedStatement preparedStatement) throws SQLException {
        TYPE value = getter.apply(item);
        if ( value == null && ! nullable ){
            throw new HrormException("Tried to set a null value for " + prefix + "." + name + " which was set not nullable.");
        }
        if ( value == null ) {
            preparedStatement.setNull(index,sqlType());
        } else {
            setPreparedStatement(preparedStatement, index, value);
        }
    }

    @Override
    public void notNull() {
        nullable = false;
    }

    abstract TYPE fromResultSet(ResultSet resultSet, String columnName) throws SQLException;

    abstract void setPreparedStatement(PreparedStatement preparedStatement, int index, TYPE value) throws SQLException;

    abstract int sqlType();
}
