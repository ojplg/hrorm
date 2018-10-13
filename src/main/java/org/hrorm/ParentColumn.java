package org.hrorm;

import java.util.function.BiConsumer;

/**
 * Represents a reference from a child entity to its parent.
 *
 * <p>
 *
 * Most users of hrorm will have no need to directly use this.
 *
 * @param <T> The child entity type
 * @param <PARENT> The type of the parent
 */
public interface ParentColumn<T,PARENT> extends TypedColumn<T> {
    void setParentPrimaryKey(PrimaryKey<PARENT> parentPrimaryKey);
    BiConsumer<T, PARENT> setter();
}
