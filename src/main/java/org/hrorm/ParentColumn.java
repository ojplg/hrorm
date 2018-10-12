package org.hrorm;

import java.util.function.BiConsumer;

public interface ParentColumn<T,PARENT> extends TypedColumn<T> {
    void setParentPrimaryKey(PrimaryKey<PARENT> parentPrimaryKey);
    BiConsumer<T, PARENT> setter();
}
