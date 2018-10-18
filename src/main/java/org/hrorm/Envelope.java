package org.hrorm;

public class Envelope<T> {

    private final T item;
    private final Long id;
    private final Long parentId;

    public Envelope(T item, Long id, Long parentId){
        if( item == null ){
            throw new HrormException("Cannot persist a null item");
        }
        this.item = item;
        this.id = id;
        this.parentId = parentId;
    }

    public Envelope(T item, Long id) {
        this(item, id, null);
    }

    public T getItem() {
        return item;
    }

    public Long getId() {
        return id;
    }

    public Long getParentId() {
        return parentId;
    }
}
