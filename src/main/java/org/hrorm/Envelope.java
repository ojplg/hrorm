package org.hrorm;

public class Envelope<T> {

    private final T item;
    private final Long id;

    public Envelope(T item, Long id) {
        this.item = item;
        this.id = id;
    }

    public T getItem() {
        return item;
    }

    public Long getId() {
        return id;
    }
}
