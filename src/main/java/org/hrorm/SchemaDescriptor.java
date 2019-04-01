package org.hrorm;

import java.util.List;

/**
 * Defines a class that can be used to build a richer schema than a
 * <code>DaoDescriptor</code> or <code>KeylessDaoDescriptor</code>.
 *
 * @param <ENTITY> The entity being modeled.
 * @param <BUILDER> The builder class of the entity being modeled.
 */
public interface SchemaDescriptor<ENTITY, BUILDER> extends DaoDescriptor<ENTITY, BUILDER> {

    /**
     * A list of column name lists, each of which should be unique.
     *
     * @return The list of column name lists.
     */
    List<List<String>> uniquenessConstraints();

}
