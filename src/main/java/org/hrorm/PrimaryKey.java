package org.hrorm;

public interface PrimaryKey<T> extends TypedColumn<T> {

    Long getKey(T item);
    String getSequenceName();
    void setKey(T item, Long id);

}
