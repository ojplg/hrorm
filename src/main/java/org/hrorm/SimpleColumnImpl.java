package org.hrorm;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class SimpleColumnImpl<TYPE, ENTITY, BUILDER> extends AbstractColumn<TYPE, ENTITY, BUILDER> {

    public SimpleColumnImpl(GenericColumn<TYPE> genericColumn,
                            String prefix,
                            String name,
                            Function<ENTITY, TYPE> getter,
                            BiConsumer<BUILDER, TYPE> setter,
                            String sqlTypeName,
                            boolean nullable) {
        super(genericColumn, prefix, name, getter, setter, sqlTypeName, nullable);
    }

    @Override
    public Column<ENTITY, BUILDER> withPrefix(String newPrefix, Prefixer prefixer) {
        return new SimpleColumnImpl(this.genericColumn,
                newPrefix,
                this.name,
                this.getter,
                this.setter,
                this.sqlTypeName,
                this.nullable);
    }
}
