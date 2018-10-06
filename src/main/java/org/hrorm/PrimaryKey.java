package org.hrorm;

/**
 * Representation of a primary key of an entity.
 *
 * @param <T> The entity represented by this key.
 */
public interface PrimaryKey<T> extends TypedColumn<T> {

    Long getKey(T item);
    String getSequenceName();
    void setKey(T item, Long id);

}
