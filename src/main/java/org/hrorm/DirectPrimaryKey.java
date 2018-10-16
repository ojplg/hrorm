package org.hrorm;

public interface DirectPrimaryKey<T> extends PrimaryKey<T>, DirectTypedColumn<T> {

    /**
     * Sets the key onto the object
     *
     * @param item the object whose key is to be set
     * @param id the primary key to assign it
     */
    void setKey(T item, Long id);

}
