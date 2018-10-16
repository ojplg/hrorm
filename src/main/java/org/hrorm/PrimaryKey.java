package org.hrorm;

/**
 * Representation of a primary key of an entity.
 *
 * <p>
 *
 * Most users of hrorm will have no need to directly use this.
 *
 * @param <T> The entity represented by this key.
 */
public interface PrimaryKey<T> extends TypedColumn<T> {

    /**
     * Extracts the primary key
     *
     * @param item The object whose key is to be extracted
     * @return the primary key
     */
    Long getKey(T item);

    /**
     * The name of the database sequence that is used to populate this key
     *
     * @return the sequence name
     */
    String getSequenceName();

}
