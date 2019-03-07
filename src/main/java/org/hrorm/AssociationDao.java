package org.hrorm;

import java.util.List;

/**
 * A DAO that can find and persist associations between entities.
 * An <code>AssociationDao</code> is used for managing many-to-many
 * relations that are stored in a third table, not by directly joining
 * the entity tables themselves.
 *
 * <p>
 *     The entity table must have exactly three columns, all integers:
 *     <ol>
 *     <li>A primary key</li>
 *     <li>A foreign key to the left entity</li>
 *     <li>A foreign key to the right entity</li>
 *     </ol>
 *
 * <p>
 *
 * Note that the names <code>LEFT</code> and <code>RIGHT</code> are
 * simply tuple names, and have nothing to do with left joins or right joins.
 *
 * @param <LEFT> One of the associated entities
 * @param <RIGHT> The other of the associated entities
 */
public interface AssociationDao<LEFT,RIGHT> {

    /**
     * Finds all the instances of <code>LEFT</code> entities
     * associated with the <code>RIGHT</code> instance passed.
     *
     * @param right The entity instance whose associates are to be selected
     * @return All the instances found
     */
    List<LEFT> selectLeftAssociates(RIGHT right);


    /**
     * Finds all the instances of <code>RIGHT</code> entities
     * associated with the <code>LEFT</code> instance passed.
     *
     * @param left The entity instance whose associates are to be selected
     * @return All the instances found
     */
    List<RIGHT> selectRightAssociates(LEFT left);

    /**
     * Persist a new association between two entities.
     *
     * @param left an instance of the <code>LEFT</code> entity to be associated
     * @param right and instance of the <code>RIGHT</code> entity to be associated
     * @return the primary key of the association record
     */
    Long insertAssociation(LEFT left, RIGHT right);

    /**
     * Remove an association from the underlying store
     *
     * @param left an instance of the <code>LEFT</code> entity whose association is to be broken
     * @param right and instance of the <code>RIGHT</code> entity whose association is to be broken
     */
    void deleteAssociation(LEFT left, RIGHT right);

}
