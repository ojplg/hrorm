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
public interface ParentColumn<CHILD,PARENT,CHILDBUILDER,PARENTBUILDER> extends IndirectTypedColumn<CHILD,CHILDBUILDER> {
    void setParentPrimaryKey(IndirectPrimaryKey<PARENT, PARENTBUILDER> parentPrimaryKey);
    BiConsumer<CHILDBUILDER, PARENT> setter();
}
