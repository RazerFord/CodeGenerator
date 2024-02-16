package org.codegenerator.resourcesgeneric;

import java.util.Objects;

public class TwoFieldsWithBounds<T, E extends T> {
    private T first;
    private E second;

    public T getFirst() {
        return first;
    }

    public void setFirst(T first) {
        this.first = first;
    }

    public E getSecond() {
        return second;
    }

    public void setSecond(E second) {
        this.second = second;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        TwoFieldsWithBounds<?, ?> twoFields = (TwoFieldsWithBounds<?, ?>) object;
        return Objects.equals(first, twoFields.first) && Objects.equals(second, twoFields.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    @Override
    public String toString() {
        return "TwoFields{" +
                "first=" + first +
                ", second=" + second +
                '}';
    }
}
