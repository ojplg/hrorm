package org.hrorm;

import java.util.function.BiConsumer;

/**
 * Represents a reference from a child entity to its parent.
 *
 * <p>
 *
 * Most users of hrorm will have no need to directly use this.
 *
 * @param <CHILD> The child entity type
 * @param <PARENT> The parent entity type
 * @param <CHILDBUILDER> The class used to construct new <code>CHILD</code> instances
 * @param <PARENTBUILDER> The class used to construct new <code>PARENT</code> instances
 */
public interface ParentColumn<CHILD,PARENT,CHILDBUILDER,PARENTBUILDER> extends Column<CHILD,CHILDBUILDER> {
    void setParentPrimaryKey(PrimaryKey<PARENT, PARENTBUILDER> parentPrimaryKey);
    BiConsumer<CHILDBUILDER, PARENT> setter();

    @Override
    default boolean isParentColumn() {
        return true;
    }

    Long getParentId(CHILD child);
}
