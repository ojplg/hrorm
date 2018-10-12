package org.hrorm;

import java.util.function.BiConsumer;

public interface ParentColumnI<T,PARENT> extends TypedColumn<T> {
    void setParentPrimaryKey(PrimaryKey<PARENT> parentPrimaryKey);
    BiConsumer<T, PARENT> setter();
}
