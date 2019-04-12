package org.hrorm;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class ConvertingColumnImpl<TYPE, DBTYPE, ENTITY, BUILDER> extends AbstractColumn<DBTYPE, ENTITY, BUILDER> {

    public ConvertingColumnImpl(GenericColumn<DBTYPE> genericColumn,
                                String prefix,
                                String name,
                                Function<ENTITY, TYPE> getter,
                                BiConsumer<BUILDER, TYPE> setter,
                                String sqlTypeName,
                                boolean nullable,
                                Converter<TYPE, DBTYPE> converter) {
        super(genericColumn, prefix, name, convertedGetter(converter, getter), convertedSetter(converter,setter), sqlTypeName, nullable);
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
    public Column<ENTITY, BUILDER> withPrefix(String newPrefix, Prefixer prefixer) {
        // NOTE: This needs to be a simple implementation.
        // Returning another ConvertingColumnImpl would mean calling the converter twice,
        // not what we want.
        return new SimpleColumnImpl(this.genericColumn,
                newPrefix,
                this.name,
                this.getter,
                this.setter,
                this.sqlTypeName,
                this.nullable);
    }
}
