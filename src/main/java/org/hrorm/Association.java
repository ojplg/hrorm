package org.hrorm;

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
