package org.hrorm;

import java.util.List;

/**
 * Implementers of this interface completely describe all the information
 * necessary to persisting objects of type <code>ENTITY</code>.
 *
 * <p>
 *
 * Most users of hrorm will have no need to directly use this.
 *
 * @param <ENTITY> The type representing the enitity being persisted.
 * @param <ENTITYBUILDER> The type of object that can build an <code>ENTITY</code> instance.
 */
public interface DaoDescriptor<ENTITY, ENTITYBUILDER> extends KeylessDaoDescriptor<ENTITY, ENTITYBUILDER> {

    /**
     * The primary key for objects of type <code>ENTITY</code>
     *
     * @return the primary key
     */
    default PrimaryKey<ENTITY, ENTITYBUILDER> primaryKey(){
        return getColumnCollection().getPrimaryKey();
    }

    /**
     * The parent column, if there is one, of the <code>ENTITY</code>.
     *
     * @param <P> The type of the parent entity.
     * @param <PB> The type of the parent entity's builder class.
     * @return The parent column.
     */
    <P,PB> ParentColumn<ENTITY, P, ENTITYBUILDER, PB> parentColumn();

    /**
     * Indicator of whether or not this entity has a parent.
     *
     * @return true if there is a parent, or false otherwise.
     */
    default boolean hasParent(){
        return parentColumn() != null;
    }

    /**
     * The definitions of any entities that are owned by type <code>ENTITY</code>
     *
     * @return all the owned entities
     */
    List<ChildrenDescriptor<ENTITY, ?, ENTITYBUILDER, ?>> childrenDescriptors();

}
