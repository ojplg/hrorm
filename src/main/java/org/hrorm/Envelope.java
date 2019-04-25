package org.hrorm;

/**
 * A simple holder for data objects that can package
 * it with its own ID.
 *
 * <p>
 *
 * Most users of hrorm will have no need to directly use this.
 *
 * @param <T> The type of the item being held.
 */
public class Envelope<T,PK> {

    private final T item;
    private final PK id;
    private final Long parentId;

    public Envelope(T item, PK id, Long parentId){
        if( item == null ){
            throw new HrormException("Cannot persist a null item");
        }
        this.item = item;
        this.id = id;
        this.parentId = parentId;
    }

    public Envelope(T item, PK id) {
        this(item, id, null);
    }

    public Envelope(T item){
        this(item, null, null);
    }

    public T getItem() {
        return item;
    }

    public PK getId() {
        return id;
    }

    public Long getParentId() {
        return parentId;
    }

    @Override
    public String toString() {
        return "Envelope{" +
                "item=" + item +
                ", id=" + id +
                ", parentId=" + parentId +
                '}';
    }
}
