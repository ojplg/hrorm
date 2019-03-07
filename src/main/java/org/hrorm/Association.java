package org.hrorm;

/**
 * A simple tuple class that binds two objects.
 *
 * <p>
 *
 * Most users of hrorm will have no need to directly use this.
 *
 * @param <LEFT> The type of the left object
 * @param <RIGHT> The type of the right object
 */
public class Association<LEFT, RIGHT> {
    private Long id;
    private LEFT left;
    private RIGHT right;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LEFT getLeft() {
        return left;
    }

    public void setLeft(LEFT left) {
        this.left = left;
    }

    public RIGHT getRight() {
        return right;
    }

    public void setRight(RIGHT right) {
        this.right = right;
    }
}
