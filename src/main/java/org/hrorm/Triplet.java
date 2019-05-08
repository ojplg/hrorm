package org.hrorm;

import java.util.Objects;

/**
 * Represents a three-tuple of objects.
 *
 * @param <T> The type of the first object.
 * @param <U> The type of the second object.
 * @param <V> The type of the third object.
 */
public class Triplet<T,U,V> {

    private final T first;
    private final U second;
    private final V third;

    public Triplet(T first, U second, V third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public T getFirst() {
        return first;
    }

    public U getSecond() {
        return second;
    }

    public V getThird() {
        return third;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Triplet<?, ?, ?> triple = (Triplet<?, ?, ?>) o;
        return Objects.equals(first, triple.first) &&
                Objects.equals(second, triple.second) &&
                Objects.equals(third, triple.third);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second, third);
    }

    @Override
    public String toString() {
        return "Triplet{first='" + first + "',second='" + second + "',third='" + third + "'}";
    }
}
