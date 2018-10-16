package org.hrorm;

public interface IndirectPrimaryKey<T,B> extends PrimaryKey<T>, IndirectTypedColumn<T,B> {

    /**
     * Sets the key onto the object
     *
     * @param item the object whose key is to be set
     * @param id the primary key to assign it
     */
    void optimisticSetKey(T item, Long id);

    void setKey(B builder, Long id);

    Long getKey(T item);
}
