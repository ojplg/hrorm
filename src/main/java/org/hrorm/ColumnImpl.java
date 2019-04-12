package org.hrorm;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * A basic implementation of the <code>Column</code> interface for data elements.
 *
 * <p>
 *
 * Most users of hrorm will have no need to directly use this.
 *
 * @param <TYPE> The type of the data element.
 * @param <ENTITY> The type of the entity.
 * @param <BUILDER> The class that is used to build new entity instances.
 */
public class ColumnImpl<TYPE, ENTITY, BUILDER> implements Column<ENTITY,BUILDER> {

    private final GenericColumn<TYPE> genericColumn;

    private final String name;
    private final String prefix;
    private final Function<ENTITY, TYPE> getter;
    private final BiConsumer<BUILDER, TYPE> setter;

    private String sqlTypeName;
    private boolean nullable;

    public ColumnImpl(GenericColumn<TYPE> genericColumn,
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

    public <MODELTYPE> ColumnImpl(GenericColumn<TYPE> genericColumn,
                               String prefix,
                               String name,
                               Function<ENTITY, MODELTYPE> getter,
                               BiConsumer<BUILDER, MODELTYPE> setter,
                               String sqlTypeName,
                               boolean nullable,
                               Converter<MODELTYPE, TYPE> converter){
        this.genericColumn = genericColumn;

        this.prefix = prefix;
        this.name = name;
        this.setter = convertedSetter(converter, setter);
        this.getter = convertedGetter(converter, getter);
        this.sqlTypeName = sqlTypeName;
        this.nullable = nullable;
    }

    private static <T, E, DB> Function<E, DB> convertedGetter(Converter<T, DB> converter, Function<E, T> rawGetter){
        return entity -> {
            T value = rawGetter.apply(entity);
            if( value == null ){
                return null;
            }
            return converter.from(value);
        };
    }

    private static <T, B, DB> BiConsumer<B, DB> convertedSetter(Converter<T, DB> converter, BiConsumer<B, T> setter){
        return (builder, dbType) -> {
            if ( dbType != null ){
                T value = converter.to(dbType);
                setter.accept(builder, value);
            }
        };
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

    @Override
    public Column<ENTITY, BUILDER> withPrefix(String newPrefix, Prefixer prefixer) {
        return new ColumnImpl(this.genericColumn,
                newPrefix,
                this.name,
                this.getter,
                this.setter,
                this.sqlTypeName,
                this.nullable);
    }
}
