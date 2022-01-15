package org.hrorm;

/**
 * Representation of the column that holds the primary key for the entity.
 *
 * <p>
 *
 * Most users of hrorm will have no need to directly use this.
 *
 * @param <ENTITY> The type of the entity being represented
 * @param <BUILDER> The type of the class that can construct new <code>ENTITY</code> instances
 */
public interface PrimaryKey<ENTITY, BUILDER> extends Column<Long, Long, ENTITY, BUILDER> {

    /**
     * Sets the key onto the object
     *
     * @param item the object whose key is to be set
     * @param id the primary key to assign it
     */
    void optimisticSetKey(ENTITY item, Long id);


    /**
     * Reads the value of the primary key from the passed entity object.
     *
     * @param item The object whose primary key is to be read.
     * @return The primary key, or null.
     */
    Long getKey(ENTITY item);

    /**
     * The name of the database sequence that is used to populate this key
     *
     * @return the sequence name
     */
    String getSequenceName();

    /**
     * Reads the value of the primary key from the passed entity object.
     * Will throw a NullPointerException if the key has not been set.
     *
     * @param item The object whose primary key is to be read.
     * @return The primary key.
     */
    default long getKeyPrimitive(ENTITY item) {
        return getKey(item).longValue();
    }

}
