package org.hrorm;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class AbstractColumnImpl<TYPE, ENTITY, BUILDER> implements Column<ENTITY, BUILDER> {

    protected final GenericColumn<TYPE> genericColumn;

    protected final String name;
    protected final String prefix;
    protected final Function<ENTITY, TYPE> getter;
    protected final BiConsumer<BUILDER, TYPE> setter;

    protected String sqlTypeName;
    protected boolean nullable;

    public AbstractColumnImpl(GenericColumn<TYPE> genericColumn,
                              String prefix,
                              String name,
                              Function<ENTITY, TYPE> getter,
                              BiConsumer<BUILDER, TYPE> setter,
                              String sqlTypeName,
                              boolean nullable){
        this.genericColumn = genericColumn;

        this.prefix = prefix;
        this.name = name;
        this.setter = setter;
        this.getter = getter;
        this.sqlTypeName = sqlTypeName;
        this.nullable = nullable;
    }

    @Override
    public void setValue(ENTITY item, int index, PreparedStatement preparedStatement) throws SQLException {
        TYPE value = getter.apply(item);
        if ( value == null && ! nullable ){
            throw new HrormException("Tried to set a null value for " + prefix + "." + name + " which was set not nullable.");
        }
        if ( value == null ) {
            preparedStatement.setNull(index,genericColumn.sqlType());
        } else {
            genericColumn.setPreparedStatement(preparedStatement, index, value);
        }
    }

    @Override
    public PopulateResult populate(BUILDER builder, ResultSet resultSet) throws SQLException {
        TYPE value = genericColumn.fromResultSet(resultSet, prefix  + name);
        setter.accept(builder, value);
        return PopulateResult.Ignore;
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
    public void notNull() {
        nullable = false;
    }

    @Override
    public boolean isNullable() {
        return nullable;
    }

    @Override
    public Set<Integer> supportedTypes() {
        return genericColumn.getSupportedTypes();
    }

    @Override
    public String getSqlTypeName() {
        return sqlTypeName;
    }

    @Override
    public void setSqlTypeName(String sqlTypeName) {
        this.sqlTypeName = sqlTypeName;
    }
}
