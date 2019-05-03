package org.hrorm;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * A basic implementation of the <code>Column</code> interface for data elements.
 *
 * <p>
 *
 * Most users of hrorm will have no need to directly use this.
 *
 * @param <DBTYPE> The type of the data element, as represented in the database.
 * @param <CLASSTYPE> The type of the data element, as represented in the Java class <code>ENTITY</code>.
 * @param <ENTITY> The type of the entity.
 * @param <BUILDER> The class that is used to build new entity instances.
 */
public class ColumnImpl<DBTYPE, CLASSTYPE, ENTITY, BUILDER> implements Column<DBTYPE, CLASSTYPE, ENTITY, BUILDER> {

    private GenericColumn<DBTYPE> genericColumn;

    private final String name;
    private final String prefix;
    private final Function<ENTITY, CLASSTYPE> getter;
    private final BiConsumer<BUILDER, CLASSTYPE> setter;

    private final Converter<CLASSTYPE, DBTYPE> converter;

    private boolean nullable;

    public static <T,E,B> Column<T,T,E,B> directColumn(GenericColumn<T> genericColumn,
                      String prefix,
                      String name,
                      Function<E, T> getter,
                      BiConsumer<B, T> setter,
                      String sqlTypeName,
                      boolean nullable){
        return new ColumnImpl<>(
                genericColumn,
                prefix,
                name,
                getter,
                setter,
                sqlTypeName,
                nullable,
                Converters.identity()
        );
    }

    public ColumnImpl(GenericColumn<DBTYPE> genericColumn,
                                  String prefix,
                                  String name,
                                  Function<ENTITY, CLASSTYPE> getter,
                                  BiConsumer<BUILDER, CLASSTYPE> setter,
                                  String sqlTypeName,
                                  boolean nullable,
                                  Converter<CLASSTYPE, DBTYPE> converter){
        this.genericColumn = genericColumn.withTypeName(sqlTypeName);

        this.prefix = prefix;
        this.name = name;
        this.setter = setter;
        this.getter = getter;
        this.nullable = nullable;
        this.converter = converter;
    }

    @Override
    public void setValue(ENTITY item, int index, PreparedStatement preparedStatement) throws SQLException {
        CLASSTYPE value = getter.apply(item);
        if ( value == null && ! nullable ){
            throw new HrormException("Tried to set a null value for " + prefix + "." + name + " which was set not nullable.");
        }
        if ( value == null ) {
            preparedStatement.setNull(index,genericColumn.sqlType());
        } else {
            DBTYPE dbValue = converter.from(value);
            genericColumn.setPreparedStatement(preparedStatement, index, dbValue);
        }
    }

    @Override
    public PopulateResult populate(BUILDER builder, ResultSet resultSet) throws SQLException {
        DBTYPE dbValue = genericColumn.fromResultSet(resultSet, prefix  + name);
        CLASSTYPE value = null;
        if ( dbValue != null ) {
            value = converter.to(dbValue);
        }
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
    public void setSqlTypeName(String sqlTypeName) {
        this.genericColumn = genericColumn.withTypeName(sqlTypeName);
    }

    @Override
    public Column<DBTYPE, CLASSTYPE, ENTITY, BUILDER> withPrefix(String newPrefix, Prefixer prefixer) {
        return new ColumnImpl(this.genericColumn,
                newPrefix,
                this.name,
                this.getter,
                this.setter,
                this.genericColumn.getSqlTypeName(),
                this.nullable,
                this.converter);
    }

    @Override
    public CLASSTYPE toClassType(DBTYPE dbType) {
        return converter.to(dbType);
    }

    public GenericColumn<DBTYPE> asGenericColumn(){
        return genericColumn;
    }
}
